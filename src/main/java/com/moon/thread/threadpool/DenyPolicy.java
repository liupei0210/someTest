package com.moon.thread.threadpool;
@FunctionalInterface
public interface DenyPolicy {
    void reject(Runnable runnable,ThradPool threadPool);
    //该拒绝策略直接将任务丢弃
    class DiscardDenyPolicy implements DenyPolicy{
        @Override
        public void reject(Runnable runnable, ThradPool threadPool) {
            //do nothing
        }
    }
    //该拒绝策略会向任务提交者抛出异常
    class AbortDenyPolicy implements DenyPolicy{

        @Override
        public void reject(Runnable runnable, ThradPool threadPool) {
            throw new RunnableDenyException("The runnable "+runnable+" will be abort");
        }
    }
    //该拒绝策略会在任务提交者所在线程中执行任务
    class RunnerDenyPolicy implements DenyPolicy{

        @Override
        public void reject(Runnable runnable, ThradPool threadPool) {
            if(!threadPool.isShutdown()){
                runnable.run();
            }
        }
    }
}
