package flightdatabase.utils

import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object CollectionsHelper extends LazyLogging {

  implicit class EitherOps[A, B](private val either: Either[A, B]) extends AnyVal {

    def foldMap[C, D](fa: A => C, fb: B => D): Either[C, D] =
      either match {
        case Left(value)  => Left(fa(value)).withRight[D]
        case Right(value) => Right(fb(value)).withLeft[C]
      }

    def toOptionWithDebug: Option[B] = either match {
      case Left(value) =>
        logger.warn(s"Encountered an error: $value")
        None
      case Right(value) =>
        logger.debug(s"Successfully obtained $value")
        Option(value)
    }
  }

  implicit def iterableToRichIterable[A](l: Iterable[A]): RichIterable[A] =
    new RichIterable(l)

  implicit def arrayToRichIterable[A](l: Array[A]): RichIterable[A] = new RichIterable(l)

  implicit def stringToRichIterable(s: String): RichIterable[Char] = new RichIterable(s)

  class RichIterable[A](private val iterable: Iterable[A]) {
    def sumBy[B](f: A => B)(implicit num: Numeric[B]): B = iterable.map(f).sum

    def averageBy[B](f: A => B)(implicit num: Numeric[B]): Option[Double] =
      Try(num.toDouble(iterable.sumBy(f)) / iterable.size).filter(_.isDefined).toOption

    // To overcome the fact that `AbstractIterable` does not have a `contains` method (wonder why!)
    // Cannot name it `contains` since IntelliJ then suggests changing `.exists(_ == element)` to `.contains(element)`,
    // which would cause an endless recursive loop!
    def containsElement[B >: A](element: B): Boolean = iterable.exists(_ == element)

    // Count operations
    // Caution: `countChanges` will not terminate for infinite-sized sequences (e.g. iterators, streams)
    def countChanges: Int = iterable.toIndexedSeq.sliding(2).count {
      case IndexedSeq(a, b) => a != b
      case _                => false // In case of sequences with length <= 1
    }

    def countChanges[B](f: A => B): Int = iterable.map(f).countChanges

    // Percentage operations
    def percentageOf(f: A => Boolean): Option[Double] =
      Try(iterable.count(f) / iterable.size.toDouble).filter(_.isDefined).toOption

    // Length comparison operations
    def lengthEquals(len: Int): Boolean = compareLength(len) == 0
    def lengthEquals[B](other: Iterable[B]): Boolean = compareLengthWith(other) == 0
    def shorterThan(len: Int): Boolean = compareLength(len) < 0
    def shorterThan[B](other: Iterable[B]): Boolean = compareLengthWith(other) < 0
    def longerThan(len: Int): Boolean = compareLength(len) > 0
    def longerThan[B](other: Iterable[B]): Boolean = compareLengthWith(other) > 0
    def equalToOrShorterThan(len: Int): Boolean = compareLength(len) <= 0
    def equalToOrShorterThan[B](other: Iterable[B]): Boolean = compareLengthWith(other) <= 0
    def equalToOrLongerThan(len: Int): Boolean = compareLength(len) >= 0
    def equalToOrLongerThan[B](other: Iterable[B]): Boolean = compareLengthWith(other) >= 0

    // Min and max operations
    def minOption(implicit ord: Ordering[A]): Option[A] =
      if (iterable.isEmpty) None else Some(iterable.min)

    def maxOption(implicit ord: Ordering[A]): Option[A] =
      if (iterable.isEmpty) None else Some(iterable.max)

    def minOf[B](f: A => B)(implicit ord: Ordering[B]): B = iterable.map(f).min
    def minOfOption[B](f: A => B)(implicit ord: Ordering[B]): Option[B] = iterable.map(f).minOption

    def maxOf[B](f: A => B)(implicit ord: Ordering[B]): B = iterable.map(f).max
    def maxOfOption[B](f: A => B)(implicit ord: Ordering[B]): Option[B] = iterable.map(f).maxOption

    def minByOption[B](f: A => B)(implicit ord: Ordering[B]): Option[A] =
      if (iterable.isEmpty) None else Some(iterable.minBy(f))

    def maxByOption[B](f: A => B)(implicit ord: Ordering[B]): Option[A] =
      if (iterable.isEmpty) None else Some(iterable.maxBy(f))

    // Private utility methods
    private def compareLengthWith[B](other: Iterable[B]): Int = {
      @tailrec def loop(itA: LazyList[A], itB: LazyList[B]): Int =
        (Try(itA.head), Try(itB.head)) match {
          case (Failure(_), Success(_)) => -1                       // Shorter
          case (Success(_), Failure(_)) => 1                        // Longer
          case (Failure(_), Failure(_)) => 0                        // Equal
          case _                        => loop(itA.tail, itB.tail) // Next iteration
        }

      loop(iterable.to(LazyList), other.to(LazyList))
    }

    // Bringing `lengthCompare` to all `Iterable` and `Array` types
    private def compareLength(len: Int): Int = {
      @tailrec def loop(i: Int, xs: Iterable[A]): Int =
        xs match {
          case t: Iterable[A] if t.isEmpty && i == len  => 0                   // Equal
          case t: Iterable[A] if t.nonEmpty && i == len => 1                   // Longer
          case t: Iterable[A] if t.isEmpty && i != len  => -1                  // Shorter
          case t: Iterable[A]                           => loop(i + 1, t.tail) // Next iteration
        }

      if (len < 0) 1 else loop(0, iterable)
    }
  }

  implicit class MoreStringOps(private val str: String) extends AnyVal {
    // Safe conversion methods
    def asInt: Option[Int] = Try(str.trim.toInt).toOption
    def asLong: Option[Long] = Try(str.trim.toLong).toOption
    def asDouble: Option[Double] = Try(str.toDouble).toOption

    def asBoolean: Option[Boolean] =
      Try(str.trim.toLowerCase).collect {
        case v: String if v == "true" || v == "1"  => true
        case v: String if v == "false" || v == "0" => false
      }.toOption

    // Other helper methods
    def toOption: Option[String] = if (str.isBlank) None else Some(str)
    def hasValue: Boolean = toOption.isDefined
    def trimEdges: String = str.tail.init
    def validRegex: Boolean = Try(str.r).isSuccess
    def removeSubstring(substr: String): String = str.replace(substr, "")
    def removeSeparator(sep: Char): String = str.replaceAll(escape(sep), "")
    def removeQuotes(): String = removeSeparator('"')
    def removeSpaces(): String = removeSeparator(' ')
    def fixDecimalNotation(sep: Char = ','): String = str.trim.replaceAll(escape(sep), ".")
    def deToEnNotation(): String = removeSeparator('.').fixDecimalNotation()
    def encodeSpacesInUrl(): String = str.replaceAll(" ", "%20")
    def encodePlusInUrl(): String = str.replaceAll("\\+", "%2B")
    def elseIfBlank(default: => String): String = if (str.isBlank) default else str

    def elseIfContains(substr: String, default: => String): String =
      if (str.contains(substr)) default else str

    def splitInTwo(by: Char): (String, String) = {
      val split = str.split(by)
      (split.head, split.last)
    }

    private def escape(char: Char): String = {
      val W = "(\\w)".r
      char match {
        case W(_) => char.toString
        case _    => s"\\$char"
      }
    }
  }

  implicit class TryOps[A](private val tryA: Try[A]) extends AnyVal {
    // Additional methods based on `Option` class
    def exists(f: A => Boolean): Boolean = tryA.isSuccess && f(tryA.get)
    def forall(f: A => Boolean): Boolean = tryA.isFailure || f(tryA.get)
    def contains[B >: A](b: B): Boolean = tryA.isSuccess && b == tryA.get
    def toList: List[A] = tryA.toOption.toList
    def toSet: Set[A] = tryA.toOption.toSet
  }

  implicit class DoubleOps(val x: Double) extends AnyVal {
    def isDefined: Boolean = !x.isNaN && !x.isInfinity
    def isUndefined: Boolean = !isDefined
  }
}
