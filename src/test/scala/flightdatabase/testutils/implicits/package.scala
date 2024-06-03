package flightdatabase.testutils

package object implicits {
  @inline implicit def enrichAny[A](a: A): RichAny[A] = new RichAny(a)
  @inline implicit def enrichtAnyCollection[C[_], A](ca: C[A]): RichAnyCollection[C, A] = new RichAnyCollection(ca)
}
