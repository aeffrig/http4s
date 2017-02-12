package org.http4s

import scala.concurrent.duration._

import cats.kernel.laws._
import org.http4s.CharsetRange._
import org.http4s.testing.ThreadDumpOnTimeout
import org.scalacheck._
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._

class CharsetRangeSpec extends Http4sSpec with ThreadDumpOnTimeout {

  // wait for completion in 200ms intervals
  override def triggerThreadDumpAfter = 4.seconds
  override def slices = 20

  "*" should {
    "be satisfied by any charset when q > 0" in {
      prop { (range: CharsetRange.`*`, cs: Charset) =>
        range.qValue > QValue.Zero ==> { range isSatisfiedBy cs }
      }
    }

    "not be satisfied when q = 0" in {
      prop { cs: Charset =>
        !(`*`.withQValue(QValue.Zero) isSatisfiedBy cs)
      }
    }
  }

  "atomic charset ranges" should {
    "be satisfied by themselves if q > 0" in {
      forAll (arbitrary[CharsetRange.Atom] suchThat (_.qValue > QValue.Zero)) { range =>
        range isSatisfiedBy range.charset
      }
    }

    "not be satisfied by any other charsets" in {
      prop { (range: CharsetRange.Atom, cs: Charset) =>
        range.charset != cs ==> { !(range isSatisfiedBy cs) }
      }
    }
  }

  checkAll("CharsetRange", OrderLaws[CharsetRange].eqv)
}
