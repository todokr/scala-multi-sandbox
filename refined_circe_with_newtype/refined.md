SoRの性質が強いBtoBアプリケーションでは、徹底的に「堅く」作ることが求められる箇所がしばしばあります。ランタイムバリデーション

refined + newtypeの話題に入る前に、これまでにどのようなテクニックが使われてきたかを簡単に振り返りましょう。
ここに、SNSのユーザーアカウントを表現するクラスがあります。

```scala
case class User(id: String, email: String, age: Int)
val user1 = User("@todokr", "tadokoro@example.com", 29)
```

さて、この素朴な実装にはどんな心配があるでしょう？真っ先に気になるのが、引数をあまりにも簡単に取り違えてしまえる点ではないでしょうか。

```scala
val invalidUser = User("tadokoro@example.com", "@todokr", 29)
println(invalidUser.email) // @todokr
```

`id` も　`email` も型は同じ `String` なので、渡す値を取り違えても問題なくコンパイルができてしまいます。

この問題に対しては、[Tagged Type](http://kxbmap.github.io/slides/scalaz-tagged-type/#0) や Value Classなどのアプローチがあります。
下記はValue Classを使う例です。

```scala
case class UserId(value: String) extends AnyVal
case class Email(value: String) extends AnyVal
case class Age(value: Int) extends AnyVal
case class User(id: UserId, email: Email, age: Age)

val userId = UserId("@todokr")
val email = Email("tadokoro@example.com")
val age = Age(29)
val user = User(email, userId, age)
// コンパイルエラー😉
// type mismatch;
//  found   : Email
//  required: UserId
```


各引数がそれぞれ独自の型を要求するようになったので、渡す値を取り違えるとコンパイルができないようになったので、上のようなミスはもう起こりません。
これで安心でしょうか？まだ気になる点があります。次の例をご覧ください。

```scala
val userId = UserId("") // 空の文字列
val email = Email("📧") // Eメールとして不正なフォーマットの文字列
val age = Age(-1) // 負数の年齢
val invalidUser = User(email, userId, age) // でたらめなユーザー😢
```

Value Classの `value` 自体は素朴な `String` や　`Int` です。Value Classの型で値の「種類」に間違いがないことは保証できても、値の「中身」に間違いがないことは保証できません。
そのため、上の例で見たように、空文字や負の数といった不正な値が入り込む余地があります。
このような不正な値が入り込むのを防ぐにはどうするのが良いでしょうか？よく使われるテクニックのひとつが、 [スマートコンストラクタパターン](https://wiki.haskell.org/Smart_constructors) です。

```scala
case class User(id: UserId, email: Email, age: Age)
case class UserId(value: String) extends AnyVal
object UserId {
  def apply(rawUserId: String): Option[UserId] = {
    Some(rawUserId).filter(isValidUserId).map(new UserId(_))
  }

  // `@`から始まって3文字以上16文字以内、@adminや@Adminは許容しない
  private def isValidUserId(s: String): Boolean =
    s.startsWith("@") && s.length >= 3 && s.length <= 16 && !s.matches("(?i)@admin")
}

case class Email(value: String) extends AnyVal
object Email {
  def apply(rawEmail: String): Option[Email] =
    Some(rawEmail).filter(isValidEmail).map(new Email(_))

  // Eメールの簡易的なバリデーションルール
  private def isValidEmail(s: String): Boolean =
    s.matches("""[a-z0-9]+@[a-z0-9]+\.[a-z0-9]{2,}""")
}

case class Age(value: Int) extends AnyVal
object Age {
  def apply(rawAge: Int): Option[Age] =
    Some(rawAge).filter(isValidAge).map(new Age(_))

  // 年齢は0以上、200以下
  private def isValidAge(i: Int): Boolean = i >= 0 && i <= 200
}

import cats.data.ValidatedNel
import cats.implicits._

def validateUser(rawUserId: String, rawEmail: String, rawAge: Int): ValidatedNel[String, User] = (
  UserId(rawUserId).toValidNel(s"Invalid UserId: $rawUserId"),
  Email(rawEmail).toValidNel(s"Invalid Email: $rawEmail"),
  Age(rawAge).toValidNel(s"Invalid Age: $rawAge")
).mapN(User)

println(validateUser("@todokr", "tadokoro@example.com", 29))
// Valid(User(UserId(@todokr),Email(tadokoro@example.com),Age(29)))

println(validateUser("@ADMIN", "tadokoro.example.com", -1))
// Invalid(NonEmptyList(Invalid UserId: @ADMIN, Invalid Email: tadokoro.example.com, Invalid Age: -1))
```

UserIdやAgeのapplyメソッドは渡された値の妥当であればSomeに包んだValue Classを、そうでなければNoneを返すようになっています。
`validateUser` メソッドではこれらValue Classのスマートコンストラクタでのインスタンス生成結果と、Catsの `.toValidNel` を用いてバリデーションを行っています。
このメソッドはID、メールアドレス、年齢の全てが妥当であれば `Valid` に包まれたUserを、そうでなければ `Invalidに包まれた` バリデーションルールを満たさなかった項目についての情報のNonEmptyListをを返します。

ここまでやればもう万全、と言いたいところですが...あと一点だけ気になることがあります！
バリデーションによって不正な値を防ぐことはできましたが、Value Classの　`value` は相変わらず `String` や `Int` のままです。
すなわち、「ユーザーIDは`@`から始まって3文字以上16文字以内、@adminや@Adminは許容しない」や「年齢は0以上、200以下」といった役立つ情報が型から失われてしまいました。
そのためコードベースが大きくなるにつれて、値がバリデート済みにも関わらず、バリデーションと同じような意味のないコードをアプリケーション内のあちこちに量産してしまうかもしれません。

```scala
val user = validateUser("@todokr", "tadokoro@example.com", 29)
...
...
...
if (!user.id.value.startsWith("@")) { // 無駄なチェック😢
  handleInvalidUserId(user)
}
```

# refined

それならば、「`@`から始まって3文字以上16文字以内」などの性質を型で表現できないでしょうか？そしてあわよくば、その型を直接バリデーションに使うことができないでしょうか？

[refined](https://github.com/fthomas/refined) というライブラリがあります。
これはRefinement Type(篩型)と呼ばれる「型 + 型の性質を示す述語」という型をScalaで利用するためのライブラリです。
簡単な例を見てみましょう。

```scala
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive

val i1: Int Refined Positive = 5
```

`Int Refined Positive` の `Positive` が「型の性質を示す述語」です。この述語でIntを "refine" しています。


```scala
val i2: Int Refined Positive = -5
// [error] Predicate failed: (-5 > 0).
// [error]   val i2: Int Refined Positive = -5
// [error]                                  ^
// [info] Int(-5) <: eu.timepit.refined.api.Refined[Int,eu.timepit.refined.numeric.Positive]?
// [info] false
```

値が述語の性質を満たさない場合はコンパイルエラーになります。分かりやすいメッセージも出力してくれるのが嬉しいですね。

`import eu.timepit.refined.W // ShapelessのWitness`
`type BitcoinAddress =
  String Refined MatchesRegex[W.`"[13][a-km-zA-HJ-NP-Z1-9]{25,34}"`.T]`

先ほどの `UserId` を、refinedを使って書き直してみます。

```scala
import eu.timepit.refined.api._
import eu.timepit.refined.auto._
import eu.timepit.refined.boolean._
import eu.timepit.refined.string._

// `@`から始まって3文字以上16文字以内、@adminや@Adminは許容しない
type UserIdRule = StartsWith["@"] And MinSize[3] And MaxSize[16] And Not[MatchesRegex["(?i)@admin"]]

val userId1: String Refined UserIdRule = "@todokr"

// 以下はコンパイルエラー
val userId2: String Refined UserIdRule = "@admin"
val userId3: String Refined UserIdRule = "todokr"
val userId4: String Refined UserIdRule = "@uryyyyyyyyyyyyyy"
```

`StartsWith["@"] And MinSize[3] And ...` を見ると分かるように述語同士は合成が可能です。
ひとつひとつの述語は小さくてシンプルですが、組み合わせることで大きくて複雑なルールも簡単に作ることができます。

# newtype

このように便利な篩型を簡単に扱えるrefinedですが、たまたま同じ型+述語を持つ2種類の値は混同ができてしまいます。

```scala
type Id = String Refined NonEmpty
type Password = String Refined NonEmpty

case class LoginInfo(id: Id, password: Password)

val id: Id = "myid123"
val password: Password = "Passw0rd!"

val loginInfo = LoginInfo(id = password, password = id) // コンパイルできてしまう😢
```

ならばRefinement TypeをValue Classでラップするのはどうでしょう？これなら混同は起きないはずです！

```scala
type IdType = String Refined NonEmpty
type PasswordType = String Refined NonEmpty

case class Id(value: IdType) extends AnyVal
case class Password(value: PasswordType) extends AnyVal
case class LoginInfo(id: Id, password: Password)
```

残念ながらこのコードはコンパイルできません。なぜならValue ClassとしてAnyValを継承したcase classは、ユーザーが定義した型をラップできないためです。
`extends AnyVal` をなくして通常のcase classにするとコンパイルはできますが、Value Classとは異なりBoxing/Unboxingのコストがあるため、この方法は避けたいところです。

ここでの切り札が [scala-newtype](https://github.com/estatico/scala-newtype) です。
scala-newtypeはランタイムのオーバーヘッドなしに値をラップするクラスを作れるようにするライブラリです。
使い方は簡単で、ラッパークラスを `@newtype` アノテーションで修飾するだけです。あとはscala-newtypeのマクロが仕事をしてくれます。

```scala
type IdType = String Refined NonEmpty
type PasswordType = String Refined NonEmpty

@newtype case class Id(value: IdType)
@newtype case class Password(value: PasswordType)
case class LoginInfo(id: Id, password: Password)

val invalidId = Id("") // 述語が `NonEmpty` なのでコンパイルエラー
val validId = Id("my1d123")
val password = Password("Passw0rd!")

val loginInfo = LoginInfo(id = password, password = validId) // コンパイルエラー😉
```

改良型Usernameとそのスマートコンストラクター（から取得RefinedTypeOps）は、以前に作成した検証ロジックに取って代わりました。これを行うことで何が得られましたか？

検証ルールは、型エイリアス定義の述語として宣言的に表現されます
戻り値の型はUsernameエイリアスです。ベースStringが検証されたという情報を保持しており、コンテキストに応じて役立つ名前が付けられています。
検証ロジックとエラーレポートは基本的に無料で提供されました
エラーメッセージは明らかに非常にロボット的です！これらは一般に開発者にとっては十分であり、必要に応じてエンドユーザーにとってより快適になるように適合させることができます。
これは、実行時のコストなしで得られました。コンパイルされたバイトコードには、のみが表示されStringます。（などの値型の改良についてIntは、ボクシングのわずかなコストしか発生しません）。

# Runtime Validation

ここまではコンパイラが誤りを検出してくれる例を見てきました。しかし不正なデータの大半は外部からやってくるため、コンパイル時に静的に検査するだけでは不十分です。
先の例のバリデーションを、refined + scala-newtype でも実装してみます。

```scala
// 述語を定義
type UserIdRule = StartsWith["@"] And MinSize[3] And MaxSize[16] And Not[MatchesRegex["(?i)@admin"]]
type EmailRule = MatchesRegex["""[a-z0-9]+@[a-z0-9]+\.[a-z0-9]{2,}"""]
type AgeRule = NonNegative

// Refinement Typeの定義
type UserIdString = String Refined UserIdRule
type EmailString = String Refined EmailRule
type AgeInt = Int Refined AgeRule

@newtype case class UserId(value: UserIdString)
object UserId {
  def apply(rawUserId: String): Either[String, UserId] =
    refineV[UserIdRule](rawUserId).map(UserId(_))
}
@newtype case class Email(value: EmailString)
object Email {
  def apply(rawEmail: String): Either[String, Email] =
    refineV[EmailRule](rawEmail).map(Email(_))
}
@newtype case class Age(value: AgeInt)
object Age {
  def apply(rawAge: Int): Either[String, Age] =
     refineV[AgeRule](rawAge).map(Age(_))
}

case class User(userId: UserId, email: Email, age: Age)

import cats.data.ValidatedNel
import cats.implicits._

def validateUser(rawUserId: String, rawEmail: String, rawAge: Int): ValidatedNel[String, User] = (
  UserId(rawUserId).toValidatedNel, // Either[String, UserId] を合成のためにValidatedNelに変換
  Email(rawEmail).toValidatedNel,
  Age(rawAge).toValidatedNel
).mapN(User)

println(validateUser("@todokr", "tadokoro@example.com", 29))
// Valid(User(UserId(@todokr),Email(tadokoro@example.com),Age(29)))

println(validateUser("@ADMIN", "tadokoro.example.com", -1))
// Invalid(NonEmptyList(Invalid UserId: @ADMIN, Invalid Email: tadokoro.example.com, Invalid Age: -1))
```

# Work with JSON: Circeと組み合わせる

```scala
trait CoercibleCodecs {

  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]
    : KeyDecoder[A] =
    KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]
    : KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])
}

object Codecs extends CoercibleCodecs {
  import io.circe.refined._ // unusedに見えるが実は使われている
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
}
```

```scala
object Example extends App {
  import Codecs._

  Iterator
    .continually(Console.in.readLine())
    .takeWhile(_ != ":quit")
    .foreach { rawMessage =>
      decode[User](rawMessage) match {
        case Left(value) => println(value)
        case Right(msg) =>
          println(s"userId: ${msg.userId}, age: ${msg.age}")

          // これをimportしておくと F[T, P]をTにunwrapしてくれる
          import eu.timepit.refined.auto.autoUnwrap
          val email: String = msg.email.value
          println(s"email: $email")
      }

      println("*" * 30)
      println()
    }
}
```

# Work with DB: Doobieと組み合わせる

doobieとdoobie-refinedモジュールを使用してfindメソッドを実装する

```scala
import doobie.implicits._
import doobie.refined.implicits._
import cats.effect.IO

final case class Account(
  accountNumber: AccountNumber,
  username: Username,
  email: EmailAddress,
  score: Score
)

final class AccountRepository(transactor: Transactor[IO]) {

  def find(accountNumber: AccountNumber): IO[Option[Account]] = {
    sql"""SELECT account_number,
         |       username,
         |       email,
         |       score
         |FROM accounts
         |WHERE account_number=$accountNumber
         |""".stripMargin
      .query[Account]
      .option
      .transact(transactor)
  }
}
```


# まとめ

- 空文字や負のIntなどで引き起こされるような、ある種のバグの不在を完全に保証することができる
- 小さくてシンプルなルールを組み合わせることで、大きく複雑なルールを簡単に作り、利用することができる
- 記述量や理解のしやすさなどの開発者体験を犠牲にせずに、上記の恩恵を享けることができる

大規模で複雑かつ、バグが許されないアプリケーションにとって、refinedとscala-newtypeは心強い味方になってくれるはずです。
