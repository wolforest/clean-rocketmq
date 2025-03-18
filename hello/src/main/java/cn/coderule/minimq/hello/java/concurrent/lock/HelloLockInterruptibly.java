package cn.coderule.minimq.hello.java.concurrent.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloLockInterruptibly {
    public static void main(String[] args) {
        style1();
        style2();
    }

    public static void style1() {
        Lock lock = new ReentrantLock();

        try {
            lock.lockInterruptibly();
            log.info("style1 lock {}", lock);
        } catch (Exception e) {
            log.error("style1 lockInterruptibly Exception", e);
        } finally {
            log.info("style1 before unlock {}", lock);
            lock.unlock();
            log.info("style1 after unlock {}", lock);
        }
    }

    public static void style2() {
        Lock lock = new ReentrantLock();

        try {
            lock.lockInterruptibly();
            log.info("style2 lock {}", lock);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("style2 lockInterruptibly Exception", e);
            } finally {
                log.info("style2 before unlock {}", lock);
                lock.unlock();
                log.info("style2 after unlock {}", lock);
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
        }
    }
}
