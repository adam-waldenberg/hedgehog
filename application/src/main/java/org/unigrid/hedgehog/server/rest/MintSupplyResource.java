/*
    Unigrid Hedgehog
    Copyright © 2021-2022 The Unigrid Foundation, UGD Software AB

    This program is free software: you can redistribute it and/or modify it under the terms of the
    addended GNU Affero General Public License as published by the The Unigrid Foundation and
    the Free Software Foundation, version 3 of the License (see COPYING and COPYING.addendum).

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
    even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Affero General Public License and the addendum for more details.

    You should have received an addended copy of the GNU Affero General Public License with this program.
    If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigrid-project/hedgehog>.
*/

package org.unigrid.hedgehog.server.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.unigrid.hedgehog.model.cdi.CDIBridgeInject;
import org.unigrid.hedgehog.model.cdi.CDIBridgeResource;
import org.unigrid.hedgehog.model.spork.MintSupply;
import org.unigrid.hedgehog.model.spork.SporkDatabase;
import org.unigrid.hedgehog.server.p2p.P2PServer;

@Slf4j
@Path("/gridspork")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MintSupplyResource extends CDIBridgeResource {
	@CDIBridgeInject
	private P2PServer p2pServer;

	@CDIBridgeInject
	private SporkDatabase sporkDatabase;

	@Path("/mint-supply") @GET
	public Response list() {
		System.out.println("list()");
		return Response.ok().entity(new ArrayList<>(Arrays.asList("A", "B", "C"))).build();
	}

	@Path("/mint-supply") @POST
	public Response set(MintSupply.SporkData data) {
		System.out.println("mint-supply set()");
		log.debug(data.toString());

		if (ObjectUtils.isNotEmpty(data.getMaxSupply())) {
			return Response.ok().build();
			//final byte[] maxSupply = data.getMaxSupply().toPlainString().getBytes(CharsetUtil.ISO_8859_1);
			//final Signature signature = new Signature(Optional.of(key), Optional.empty());

			//System.out.println(p2pServer);
		}

		return Response.status(Status.BAD_REQUEST.getStatusCode(), "Missing 'maxSupply'").build();
	}
}
