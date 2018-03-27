package com.swatt.util.net;

public enum ServiceState {
	PRE_INITIALIZED, INITIALIZED, RUNNING, PAUSED, STOPPED, DESTROYED;
	
	public boolean isValidTransition(ServiceState other) {
		if ((this == PRE_INITIALIZED)  && (other == INITIALIZED))
			return true;
		else if ((this == INITIALIZED)  && (other == RUNNING))
			return true;
		else if ((this == RUNNING)  && (other == PAUSED))
			return true;
		else if ((this == RUNNING)  && (other == STOPPED))
			return true;
		else if ((this == PAUSED)  && (other == STOPPED))
			return true;
		else if ((this == STOPPED)  && (other == DESTROYED))
			return true;
		else
			return false;
	}
}
