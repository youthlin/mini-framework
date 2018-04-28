package com.youthlin.aop.core;

/**
 * 创建: youthlin.chen
 * 时间: 2018-04-28 17:41
 */
public class AdviceAdapter implements Advice {
    protected Object[] EMPTY_ARRAY = new Object[0];

    public JoinPointImpl process(JoinPointImpl pjp) throws Throwable {
        before(pjp);
        try {
            pjp = doInvoke(pjp);
            after(pjp);
        } catch (Throwable t) {
            pjp.setThrowable(t);
            onException(pjp);
        } finally {
            onDone(pjp);
        }
        return pjp;
    }

    protected void before(JoinPointImpl pjp) throws Throwable {
    }

    protected JoinPointImpl doInvoke(JoinPointImpl pjp) throws Throwable {
        return pjp;
    }

    protected void after(JoinPointImpl pjp)throws Throwable  {
    }

    protected void onException(JoinPointImpl pjp) throws Throwable {
    }

    protected void onDone(JoinPointImpl pjp) throws Throwable {
    }

}
