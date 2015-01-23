/**
 *  Good Night
 *
 *  Author: dpvorster
 *  Date: 2014-12-27
 */
definition(
    name: "Good Night",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Changes mode when no power is detected on a switch after a specific time at night.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/good-night.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/good-night@2x.png"
)

preferences {
	section("When there is no power consumed by this device") {
		input "switch1", "capability.powerMeter", title: "Where?"
	}
	section("After this time of day") {
		input "timeOfDay", "time", title: "Time?"
	}
    section("Only when mode is") {
    	input "modes", "mode", title: "Modes?", multiple: true, required: false
    }
}

def installed() 
{
	initialize()
}

def updated() 
{
	initialize()
}

def initialize()
{   
	if (now() < timeToday(timeOfDay, location.timeZone).time) {
    	resetSchedule()
    }
    else {
    	setSchedule()
    }
}

// Start polling more frequently
private setSchedule()
{
	unschedule()
    state.wasOn = (switch1.currentValue('power') > 5)
    log.debug "Setting schedule for short intervals"
	schedule("0 0/1 * * * ?", 'scheduleCheck')
}

// Reset schedule for the next day
private resetSchedule()
{
	unschedule()
    schedule(timeOfDay, 'setSchedule')
    state.wasOn = false
    log.debug "Setting schedule to run at $timeOfDay"
}

def scheduleCheck()
{
	log.debug "scheduleCheck, wasOn=$state.wasOn"
    
    // Check if switch is on
    if (! state.wasOn) {
    	state.wasOn = (switch1.currentValue('power') > 5)
    }
	
	if (correctTime() && correctMode() && state.wasOn)
    {
		if (isPowerOff())
        {
			takeActions()
		}
	}
} 

private takeActions() 
{
    log.debug "Executing good night"
	location.helloHome.execute("Good Night!")
    resetSchedule()
}

private correctTime() 
{
	def t0 = now()
	def startTime = timeToday(timeOfDay, location.timeZone)
	if (t0 >= startTime.time) {
		true
	} else {
		log.debug "The current time of day ($t0), is not in the correct time window ($startTime):  doing nothing"
		false
	}
}

private correctMode()
{
	def result = !modes || modes.contains(location.mode)
	log.debug "correctMode = $result"
	result
}

private isPowerOff() 
{
	def power = switch1.currentValue('power')
	log.debug "Power is $power"
	power < 5
}
