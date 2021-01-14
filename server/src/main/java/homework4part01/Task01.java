package homework4part01;

public class Task01 {
    private final static Object LOCK = new Object();
    private static int order = 1;

    private static class Task implements Runnable {
        private int number;
        private char letter;

        public Task(int number, char letter) {
            this.number = number;
            this.letter = letter;
        }

        @Override
        public void run() {
            synchronized (LOCK) {
                for (int i = 0; i < 5; i++) {
                    while (order != number) {
                        try {
                            LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.print(letter);
                    order = order == 3 ? 1 : order + 1;
                    LOCK.notifyAll();
                }
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Task(1, 'A')).start();
        new Thread(new Task(2, 'B')).start();
        new Thread(new Task(3, 'C')).start();
    }

}
