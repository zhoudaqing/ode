package org.apache.ode.bpel.engine;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.engine.MyRoleMessageExchangeImpl.ResponseCallback;
import org.apache.ode.scheduler.simple.SimpleScheduler;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class MyRoleMessageExchangeImplTest extends MockObjectTestCase {
    private Mock mexDao;
    
    private TestMyRoleMessageExchangeImpl myRoleMexImpl;
    Contexts contexts;
    BpelEngineImpl engine;
    TransactionManager _txm;
    
    public void testResponseReceived() throws Exception {
        mexDao.expects(atLeastOnce()).method("getDirection").will(returnValue(MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE));
        mexDao.expects(atLeastOnce()).method("getMessageExchangeId").will(returnValue("163"));
        mexDao.expects(atLeastOnce()).method("getCorrelationId").will(returnValue("corrId"));
        mexDao.expects(atLeastOnce()).method("getCorrelationStatus").will(returnValue("MATCHED"));
        mexDao.expects(atLeastOnce()).method("getPattern").will(returnValue("PATTERN"));
        mexDao.expects(atLeastOnce()).method("getCallee").will(returnValue(new QName("CALLEE")));
        mexDao.expects(atLeastOnce()).method("getStatus").will(returnValue("10"));
        mexDao.expects(atLeastOnce()).method("getOperation").will(returnValue("10"));
        mexDao.expects(atLeastOnce()).method("getPipedMessageExchangeId").will(returnValue("163"));
        mexDao.expects(atLeastOnce()).method("getFault").will(returnValue(null));
        mexDao.expects(atLeastOnce()).method("getResponse").will(returnValue(null));
        
        final boolean[] responded = new boolean[1];
        myRoleMexImpl.callbacks().put("corrId", new ResponseCallback() {
            synchronized boolean responseReceived() {
                responded[0] = true;
                return true;
            }

            synchronized void waitResponse(long timeout) {
            }
        });
        
        _txm.begin();
        myRoleMexImpl.responseReceived();
        _txm.rollback();
        
        _txm.begin();
        myRoleMexImpl.responseReceived();
        _txm.rollback();
        
        _txm.begin();
        myRoleMexImpl.responseReceived();
        _txm.commit();
        
        assertTrue(responded[0]);
    }
    
    public void testResponseTimeout() throws Exception {
        mexDao.expects(atLeastOnce()).method("getDirection").will(returnValue(MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE));
        mexDao.expects(atLeastOnce()).method("getMessageExchangeId").will(returnValue("163"));
        mexDao.expects(atLeastOnce()).method("getCorrelationId").will(returnValue("corrId"));
        mexDao.expects(atLeastOnce()).method("getCorrelationStatus").will(returnValue("MATCHED"));
        mexDao.expects(atLeastOnce()).method("getPattern").will(returnValue("PATTERN"));
        mexDao.expects(atLeastOnce()).method("getCallee").will(returnValue(new QName("CALLEE")));
        mexDao.expects(atLeastOnce()).method("getStatus").will(returnValue("10"));
        mexDao.expects(atLeastOnce()).method("getOperation").will(returnValue("10"));
        mexDao.expects(atLeastOnce()).method("getPipedMessageExchangeId").will(returnValue("163"));
        mexDao.expects(atLeastOnce()).method("getFault").will(returnValue(null));
        mexDao.expects(atLeastOnce()).method("getResponse").will(returnValue(null));

        myRoleMexImpl.callbacks().put("corrId", new MyRoleMessageExchangeImpl.ResponseCallback());

        _txm.begin();
        myRoleMexImpl.responseReceived();
        _txm.rollback();

        try {
            new MyRoleMessageExchangeImpl.ResponseFuture("corrId").get(10, TimeUnit.MILLISECONDS);
            fail("Should throw a TimeoutException!!");
        } catch( TimeoutException te ) {}
    }
    
    protected void setUp() throws Exception {
        _txm = new GeronimoTransactionManager();
        
        mexDao = new Mock(MessageExchangeDAO.class);
        SimpleScheduler scheduler = new SimpleScheduler("node", null, new Properties());
        scheduler.setTransactionManager(_txm);
        
        contexts = new Contexts();
        contexts.scheduler = scheduler;
        engine = new BpelEngineImpl(contexts);

        myRoleMexImpl = new TestMyRoleMessageExchangeImpl();
    }

    class TestMyRoleMessageExchangeImpl extends MyRoleMessageExchangeImpl {
        public TestMyRoleMessageExchangeImpl() {
            super(null, engine, (MessageExchangeDAO)mexDao.proxy());
        }
        
        public Map<String, ResponseCallback> callbacks() {
            return _waitingCallbacks;
        }
    }
}