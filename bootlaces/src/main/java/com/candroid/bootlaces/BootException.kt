package com.candroid.bootlaces

/*Exception to be thrown when no service is provided*/
class BootException: Exception("No boot service was found. Unable to start boot service. Please pass the name of your BootService to startBoot")