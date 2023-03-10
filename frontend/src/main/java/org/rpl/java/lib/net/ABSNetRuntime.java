/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.net;

import org.rpl.backend.java.lib.net.msg.CallMsg;
import org.rpl.backend.java.lib.runtime.ABSFut;
import org.rpl.backend.java.lib.runtime.ABSRuntime;
import org.rpl.backend.java.lib.runtime.AsyncCall;
import org.rpl.backend.java.lib.runtime.COG;
import org.rpl.backend.java.lib.types.ABSRef;
import org.rpl.backend.java.lib.types.ABSValue;
import org.rpl.backend.java.lib.types.ABSInterface;

/**
 * A NET-aware ABSRuntime
 * 
 * @author Jan Schäfer
 *
 */
public class ABSNetRuntime extends ABSRuntime {
    
    private final Network network;

    public ABSNetRuntime(Network network) {
        this.network = network;
    }

    @Override
    public void doNextStep() {
        System.out.println("node: "+getCurrentNode().getId());
        super.doNextStep();
    }
    
    @Override
    public COG createCOG(Class<?> clazz, ABSInterface dc) {
        // KLUDGE: we ignore "dc" here (needed to implement "thisDC()")
        return createCOGAtNode(clazz, getCurrentNode());
    }
    
    public NetCOG createCOGAtNode(Class<?> clazz, NetNode node) {
        return new NetCOG(node,this,clazz);
    }

    public NetNode getCurrentNode() {
        NetCOG cog = getCurrentNetCOG();
        if (cog != null) {
            return cog.getNode();
        } else {
            return network.getStartNode();
        }
    }
    
    public NetCOG getCurrentNetCOG() {
        return (NetCOG) getCurrentCOG();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T extends ABSRef> ABSFut<?> asyncCall(AsyncCall<T> call) {
        Promise p = new PromiseImpl();
        NetFut<? super ABSValue> fut = null;
        if (getCurrentNetCOG() != null) {
            fut = new NetFut(p);
            getCurrentNetCOG().registerFuture(fut);
        }
        getCurrentNode().processMsg(new CallMsg(p,call));
        return fut;
    }
    
}
