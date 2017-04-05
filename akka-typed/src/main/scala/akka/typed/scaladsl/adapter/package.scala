/**
 * Copyright (C) 2016-2017 Lightbend Inc. <http://www.lightbend.com/>
 */
package akka.typed
package scaladsl

import akka.annotation.InternalApi
import akka.typed.internal.adapter._

/**
 * Scala API: Adapters between typed and untyped actors and actor systems.
 * The underlying `ActorSystem` is the untyped [[akka.actor.ActorSystem]]
 * which runs Akka Typed [[akka.typed.Behavior]] on an emulation layer. In this
 * system typed and untyped actors can coexist.
 *
 * Use these adapters with `import akka.typed.scaladsl.adapter._`.
 *
 * Implicit extension methods are added to untyped and typed `ActorSystem`,
 * `ActorContext`. Such methods make it possible to create typed child actor
 * from untyped parent actor, and the opposite untyped child from typed parent.
 * `watch` is also supported in both directions.
 *
 * There is also an implicit conversion from untyped [[akka.actor.ActorRef]] to
 * typed [[akka.typed.ActorRef]].
 */
package object adapter {

  import language.implicitConversions

  /**
   * Extension methods added to [[akka.actor.ActorSystem]].
   */
  implicit class UntypedActorSystemOps(val sys: akka.actor.ActorSystem) extends AnyVal {
    def spawnAnonymous[T](behavior: Behavior[T], deployment: DeploymentConfig = EmptyDeploymentConfig): ActorRef[T] =
      ActorRefAdapter(sys.actorOf(PropsAdapter(Behavior.validateAsInitial(behavior), deployment)))
    def spawn[T](behavior: Behavior[T], name: String, deployment: DeploymentConfig = EmptyDeploymentConfig): ActorRef[T] =
      ActorRefAdapter(sys.actorOf(PropsAdapter(Behavior.validateAsInitial(behavior), deployment), name))

    def typed: ActorSystem[Nothing] = ActorSystemAdapter(sys)
  }

  /**
   * Extension methods added to [[akka.typed.ActorSystem]].
   */
  implicit class TypedActorSystemOps(val sys: ActorSystem[_]) extends AnyVal {
    def untyped: akka.actor.ActorSystem = ActorSystemAdapter.toUntyped(sys)
  }

  /**
   * Extension methods added to [[akka.actor.ActorContext]].
   */
  implicit class UntypedActorContextOps(val ctx: akka.actor.ActorContext) extends AnyVal {
    def spawnAnonymous[T](behavior: Behavior[T], deployment: DeploymentConfig = EmptyDeploymentConfig): ActorRef[T] =
      ActorContextAdapter.spawnAnonymous(ctx, behavior, deployment)
    def spawn[T](behavior: Behavior[T], name: String, deployment: DeploymentConfig = EmptyDeploymentConfig): ActorRef[T] =
      ActorContextAdapter.spawn(ctx, behavior, name, deployment)

    def watch[U](other: ActorRef[U]): Unit = ctx.watch(ActorRefAdapter.toUntyped(other))
    def unwatch[U](other: ActorRef[U]): Unit = ctx.unwatch(ActorRefAdapter.toUntyped(other))

    def stop(child: ActorRef[_]): Unit =
      ctx.stop(ActorRefAdapter.toUntyped(child))
  }

  /**
   * Extension methods added to [[akka.typed.scaladsl.ActorContext]].
   */
  implicit class TypedActorContextOps(val ctx: scaladsl.ActorContext[_]) extends AnyVal {
    def actorOf(props: akka.actor.Props): akka.actor.ActorRef =
      ActorContextAdapter.toUntyped(ctx).actorOf(props)
    def actorOf(props: akka.actor.Props, name: String): akka.actor.ActorRef =
      ActorContextAdapter.toUntyped(ctx).actorOf(props, name)

    // watch, unwatch and stop not needed here because of the implicit ActorRef conversion
  }

  /**
   * Extension methods added to [[akka.typed.ActorRef]].
   */
  implicit class TypedActorRefOps(val ref: ActorRef[_]) extends AnyVal {
    def untyped: akka.actor.ActorRef = ActorRefAdapter.toUntyped(ref)
  }

  /**
   * Implicit conversion from untyped [[akka.actor.ActorRef]] to typed [[akka.typed.ActorRef]].
   */
  implicit def actorRefAdapter(ref: akka.actor.ActorRef): ActorRef[Any] = ActorRefAdapter(ref)

}
