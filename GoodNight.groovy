/**
 *  Good Night
 *
 *  Author: dpvorster
 *  Date: 2015-07-07
 */
definition(
    name: "Good Night",
    namespace: "dpvorster",
    author: "dpvorster",
    description: "Changes mode when no power is detected on a switch after a specific time at night.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/good-night.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/good-night@2x.png"
)

preferences 
{
	section("When there is no power consumed by this device") {
		input "switch1", "capability.powerMeter", title: "Where?"
	}
	section("After this time of day") {
		input "startTime", "time", title: "Time?", required: true
	}
	section("Until this time of day") {
		input "endTime", "time", title: "Time?", required: true
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
	unsubscribe()
	subscribe (switch1, "power", eventHandler)
    log.debug "Subscribed to power event for $switch1"
}

def eventHandler(evt)
{
	if (!correctTime() || !correctMode())
    {
    	log.debug "eventHandler, nothing to do"
    	return
    }
    
	log.debug "eventHandler, wasOn=$state.wasOn"
    
    // Check if switch is on
    if (! state.wasOn) 
    {
    	state.wasOn = (switch1.currentValue('power') > 5)
    }
	
	if (state.wasOn)
    {
		if (isPowerOff())
        {
			takeActions()
            state.wasOn = false;
		}
	}
} 

private takeActions() 
{
    log.debug "Executing good night"
	location.helloHome.execute("Good Night!")
}

private correctTime() 
{
	def t0 = now()
	def start = timeToday(startTime, location.timeZone)
	def end = timeToday(endTime, location.timeZone)
    
    def result = end.time < start.time ? (t0 >= start.time || t0 < end.time) : (t0 >= start.time && t0 <= end.time)
    log.debug "correctTime = $result"
	result
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
