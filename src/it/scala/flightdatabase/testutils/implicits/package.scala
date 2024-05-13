package flightdatabase.testutils

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase.domain.ApiResult
import org.scalactic.Equality
import org.scalatest.enablers.Aggregating
import org.scalatest.enablers.Collecting
import org.scalatest.enablers.Containing

package object implicits {

  @inline implicit def enrichIOOperation[A](op: IO[ApiResult[A]]): RichIOOperation[A] =
    new RichIOOperation(op)

  implicit def collectingNel[A]: Collecting[A, Nel[A]] =
    new Collecting[A, Nel[A]] {
      type C = Nel[A]

      override def loneElementOf(collection: C): Option[A] =
        if (collection.tail.isEmpty) Some(collection.head) else None
      override def sizeOf(collection: C): Int = collection.size
      override def genTraversableFrom(collection: C): Iterable[A] = collection.toList
    }

  implicit def aggregatingNel[A](implicit listAgg: Aggregating[List[A]]): Aggregating[Nel[A]] =
    new Aggregating[Nel[A]] {

      override def containsAtLeastOneOf(aggregation: Nel[A], seq: collection.Seq[Any]): Boolean =
        listAgg.containsAtLeastOneOf(aggregation.toList, seq)

      override def containsTheSameElementsAs(
        leftAggregation: Nel[A],
        rightAggregation: Iterable[Any]
      ): Boolean = listAgg.containsTheSameElementsAs(leftAggregation.toList, rightAggregation)

      override def containsOnly(aggregation: Nel[A], seq: collection.Seq[Any]): Boolean =
        listAgg.containsOnly(aggregation.toList, seq)

      override def containsAllOf(aggregation: Nel[A], seq: collection.Seq[Any]): Boolean =
        listAgg.containsAllOf(aggregation.toList, seq)

      override def containsAtMostOneOf(aggregation: Nel[A], seq: collection.Seq[Any]): Boolean =
        listAgg.containsAtMostOneOf(aggregation.toList, seq)
    }

  implicit def containingNel[A](implicit equality: Equality[A]): Containing[Nel[A]] =
    new Containing[Nel[A]] {

      override def contains(container: Nel[A], element: Any): Boolean =
        container.exists(equality.areEqual(_, element))

      override def containsOneOf(container: Nel[A], elements: collection.Seq[Any]): Boolean =
        container.exists(c => elements.exists(equality.areEqual(c, _)))

      override def containsNoneOf(container: Nel[A], elements: collection.Seq[Any]): Boolean =
        !elements.exists(e => container.exists(equality.areEqual(_, e)))
    }

}
