/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.net.msg;

import org.rpl.backend.java.lib.net.Promise;
import org.rpl.backend.java.lib.net.NetCOG;
import org.rpl.backend.java.lib.runtime.ABSObject;
import org.rpl.backend.java.lib.runtime.AsyncCall;
import org.rpl.backend.java.lib.types.ABSRef;

public class CallMsg implements ObjectTargetMsg {
    public final AsyncCall<? extends ABSRef> call;
    public final Promise promise;

    public CallMsg(Promise promise, AsyncCall<? extends ABSRef> call) {
        this.call = call;
        this.promise = promise;
    }
    
    @Override
    public ABSObject getTarget() {
        return (ABSObject) call.getTarget();
    }

    @Override
    public NetCOG getCOG() {
	return (NetCOG) getTarget().getCOG();
    }
}
