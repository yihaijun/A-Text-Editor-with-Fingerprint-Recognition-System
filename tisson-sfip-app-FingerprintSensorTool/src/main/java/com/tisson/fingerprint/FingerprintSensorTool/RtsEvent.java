package com.tisson.fingerprint.FingerprintSensorTool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RtsEvent {
	private final ReentrantLock lock = new ReentrantLock();
//	private final Condition notFull = lock.newCondition();//写线程条件 
//	private final Condition notEmpty = lock.newCondition();//读线程条件 

	public RtsEvent(){
	}

	public void lockInterruptibly() {
		try {
			lock.lockInterruptibly();
		} catch (Throwable t) {
			log.warn("", t);
		}
	}

	public int waitEvent(int millisecs) {
		try {
			if (lock.tryLock(millisecs, TimeUnit.MILLISECONDS)) {
				return 0;
			}
		} catch (Throwable t) {
			log.warn("", t);
		}
		return -1;
	}

	public void setEvent() {
		try {
			while(lock.getHoldCount()>0){
				if(log.isTraceEnabled()){
					log.trace("lock.getHoldCount()="+lock.getHoldCount());
				}
				try {
					lock.unlock();
				} catch (Throwable t) {
					log.warn("", t);
				}
			}
		} catch (Throwable t) {
			log.warn("", t);
		}
	}

	public int getHoldCount() {
		return lock.getHoldCount();
	}

	public int getQueueLength() {
		return lock.getQueueLength();
	}
}
