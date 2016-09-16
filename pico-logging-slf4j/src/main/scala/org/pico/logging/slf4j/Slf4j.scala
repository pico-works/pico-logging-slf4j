package org.pico.logging.slf4j

import java.util.concurrent.atomic.AtomicReference

import org.pico.atomic.syntax.std.atomicReference._
import org.pico.disposal.Disposer
import org.pico.disposal.std.autoCloseable._
import org.pico.logging._
import org.slf4j.{Logger => Slf4jLogger, _}

import scala.annotation.tailrec

trait Slf4j {
  Slf4j
}

object Slf4j {
  private val disposer = Disposer()
  private val loggersRef = new AtomicReference[Map[Class[_], Slf4jLogger]](Map.empty)

  @tailrec
  private def getLogger(runtimeClass: Class[_]): Slf4jLogger = {
    val loggers = loggersRef.get()

    loggers.get(runtimeClass) match {
      case Some(logger) => logger
      case None =>
        val logger = LoggerFactory.getLogger(runtimeClass)
        loggersRef.update(_ + (runtimeClass -> logger))
        getLogger(runtimeClass)
    }
  }

  disposer += LogBus.subscribe { logEvent =>
    val logger = getLogger(logEvent.clazz)

    if (logEvent.exception != null) {
      logEvent.level match {
        case LogDebugLevel  => logger.debug(logEvent.message, logEvent.exception)
        case LogTraceLevel  => logger.trace(logEvent.message, logEvent.exception)
        case LogInfoLevel   => logger.info(logEvent.message, logEvent.exception)
        case LogWarnLevel   => logger.warn(logEvent.message, logEvent.exception)
        case LogErrorLevel  => logger.error(logEvent.message, logEvent.exception)
      }
    } else {
      logEvent.level match {
        case LogDebugLevel  => logger.debug(logEvent.message)
        case LogTraceLevel  => logger.trace(logEvent.message)
        case LogInfoLevel   => logger.info(logEvent.message)
        case LogWarnLevel   => logger.warn(logEvent.message)
        case LogErrorLevel  => logger.error(logEvent.message)
      }
    }
  }
}
