import Models._
import eu.timepit.refined.api._
import eu.timepit.refined.boolean.{And, Or}
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import eu.timepit.refined.string._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.estatico.newtype.Coercible
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._

object Models {
  type AccountIdRule =
    MinSize[3] And MaxSize[10] And StartsWith["@"] // AccountIdは 3 ~ 10文字かつ先頭が@
  type AgeRule = NonNegative // Ageは自然数
  type IpAddressRule = IPv4 Or IPv6 // IpAddressはv4かv6

  type AccountIdString = String Refined AccountIdRule
  type AgeInt = Int Refined AgeRule
  type IpAddressString = String Refined IpAddressRule

  @newtype case class AccountId(value: AccountIdString)
  @newtype case class Age(value: AgeInt)
  @newtype case class IpAddress(value: IpAddressString)
  case class Account(accountId: AccountId, age: Age, ipAddress: IpAddress)
}

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
  implicit val messageEncoder: Encoder[Account] = deriveEncoder[Account]
  implicit val messageDecoder: Decoder[Account] = deriveDecoder[Account]
}

object Example extends App {
  import Codecs._

  Iterator
    .continually(Console.in.readLine())
    .takeWhile(_ != ":quit")
    .foreach { rawMessage =>
      decode[Account](rawMessage) match {
        case Left(value) => println(value)
        case Right(msg) =>
          println(s"accountId: ${msg.accountId}, age: ${msg.age}")

          // これをimportしておくと F[T, P]をTにunwrapしてくれる
          import eu.timepit.refined.auto.autoUnwrap
          val ip: String =
            msg.ipAddress.value // autoUnwrapがないと msg.body.value.value
          println(s"from: $ip")
      }

      println("*" * 30)
      println()
      import eu.timepit.refined.auto._

      val accountId = AccountId("#todokr")
      val age = Age(-1)

      println(accountId)
      println(age)
    }
}
