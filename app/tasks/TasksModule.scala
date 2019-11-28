package tasks

import dao.CleanupTask
import play.api.inject.SimpleModule
import play.api.inject._

class TasksModule extends SimpleModule(bind[CleanupTask].toSelf.eagerly())
