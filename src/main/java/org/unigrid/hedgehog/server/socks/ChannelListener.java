package org.unigrid.hedgehog.server.socks;

import io.netty.channel.ChannelHandlerContext;

public interface ChannelListener {

	public void inActive(ChannelHandlerContext ctx);
	
	public void active(ChannelHandlerContext ctx);
}
