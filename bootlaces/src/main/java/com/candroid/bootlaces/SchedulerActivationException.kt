package com.candroid.bootlaces

import java.lang.Exception
import java.lang.IllegalStateException

class SchedulerActivationException: IllegalStateException("Must activate scheduler before scheduling.")