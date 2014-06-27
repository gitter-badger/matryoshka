package slamdata.engine.analysis

import scalaz.{Tree => ZTree, Validation, Semigroup, NonEmptyList, Foldable1, Show, Cord}

import scalaz.syntax.traverse._

import scalaz.std.vector._
import scalaz.std.list._
import scalaz.std.tuple._

import scala.collection.JavaConverters._

import slamdata.engine.{RenderTree, Terminal, NonTerminal}

sealed trait AnnotatedTree[N, A] extends Tree[N] { self =>
  def attr(node: N): A
}

trait AnnotatedTreeInstances {
  implicit def AnnotatedTreeRenderTree[N, A](implicit RN: RenderTree[N], RA: RenderTree[A]) = new RenderTree[AnnotatedTree[N, A]] {
    override def render(t: AnnotatedTree[N, A]) = {
      def renderNode(n: N): NonTerminal =
        NonTerminal(RN.render(n).label,
          RA.render(t.attr(n)).relabel("<annotation>") :: 
          t.children(n).map(renderNode(_)))

      renderNode(t.root)
    }
  }
}

object AnnotatedTree extends AnnotatedTreeInstances {
  def unit[N](root0: N, children0: N => List[N]): AnnotatedTree[N, Unit] = const(root0, children0, Unit)

  def const[N, A](root0: N, children0: N => List[N], const: A): AnnotatedTree[N, A] = new AnnotatedTree[N, A] {
    def root: N = root0

    def children(node: N): List[N] = children0(node)

    def attr(node: N): A = const
  }

  def apply[N, A](root0: N, children0: N => List[N], attr0: N => A): AnnotatedTree[N, A] = new AnnotatedTree[N, A] {
    def root: N = root0

    def children(node: N): List[N] = children0(node)

    def attr(node: N): A = attr0(node)
  }
}