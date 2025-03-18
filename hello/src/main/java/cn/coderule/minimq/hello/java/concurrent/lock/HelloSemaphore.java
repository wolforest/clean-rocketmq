package cn.coderule.minimq.hello.java.concurrent.lock;

import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloSemaphore {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);

        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName() + " acquired the semaphore.");

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("InterruptedException", e);
                } finally {
                    if (semaphore.availablePermits() == 0) {
                        System.out.println("\n\n\n");
                    }

                    semaphore.release();
                    System.out.println(Thread.currentThread().getName() + " released the semaphore.");
                }
            }).start();
        }
    }
}
