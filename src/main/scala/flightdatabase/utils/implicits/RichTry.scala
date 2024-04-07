package flightdatabase.utils.implicits

import scala.util.Try

class RichTry[A](private val tryA: Try[A]) extends AnyVal {
  // Additional methods based on `Option` class
  def exists(f: A => Boolean): Boolean = tryA.isSuccess && f(tryA.get)
  def forall(f: A => Boolean): Boolean = tryA.isFailure || f(tryA.get)
  def contains[B >: A](b: B): Boolean = tryA.isSuccess && b == tryA.get
  def toList: List[A] = tryA.toOption.toList
  def toSet: Set[A] = tryA.toOption.toSet
}
