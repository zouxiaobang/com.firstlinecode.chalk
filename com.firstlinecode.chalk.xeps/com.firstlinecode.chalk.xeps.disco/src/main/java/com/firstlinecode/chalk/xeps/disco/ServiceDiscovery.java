package com.firstlinecode.chalk.xeps.disco;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.chalk.IChatServices;
import com.firstlinecode.chalk.ISyncTask;
import com.firstlinecode.chalk.IUnidirectionalStream;
import com.firstlinecode.chalk.core.ErrorException;
import com.firstlinecode.basalt.xeps.disco.DiscoInfo;
import com.firstlinecode.basalt.xeps.disco.DiscoItems;
import com.firstlinecode.basalt.xeps.disco.Identity;
import com.firstlinecode.basalt.xeps.disco.Item;

public class ServiceDiscovery implements IServiceDiscovery {
	private IChatServices chatServices;

	@Override
	public boolean discoImServer() throws ErrorException {
		return chatServices.getTaskService().execute(new ISyncTask<Iq, Boolean>() {
			
			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				JabberId host = JabberId.parse(chatServices.getStream().getStreamConfig().getHost());
				Iq iq = new Iq();
				iq.setTo(host);
				iq.setObject(new DiscoInfo());
				
				stream.send(iq);
			}
			
			@Override
			public Boolean processResult(Iq iq) {
				DiscoInfo discoInfo = iq.getObject();
				for (Identity identity : discoInfo.getIdentities()) {
					if ("server".equals(identity.getCategory()) && "im".equals(identity.getType())) {
						return true;
					}
				}
				
				return false;
			}
		});
	}

	@Override
	public boolean discoAccount(final JabberId account) throws ErrorException {
		return chatServices.getTaskService().execute(new ISyncTask<Iq, Boolean>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Iq iq = new Iq();
				iq.setTo(account);
				iq.setObject(new DiscoInfo());
				
				stream.send(iq);
			}

			@Override
			public Boolean processResult(Iq iq) {
				DiscoInfo discoInfo = iq.getObject();
				if (discoInfo.getIdentities() != null && !discoInfo.getIdentities().isEmpty()) {
					return isAccount(discoInfo.getIdentities().get(0));
				}
				
				return false;
			}

			private boolean isAccount(Identity identity) {
				return "account".equals(identity.getCategory()) && "registered".equals(identity.getType());
			}
		});
	}

	@Override
	public JabberId[] discoAvailableResources(final JabberId account) throws ErrorException {
		return chatServices.getTaskService().execute(new ISyncTask<Iq, JabberId[]>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				Iq iq = new Iq();
				iq.setTo(account);
				iq.setObject(new DiscoItems());
				
				stream.send(iq);
			}

			@Override
			public JabberId[] processResult(Iq iq) {
				DiscoItems discoItems = iq.getObject();
				if (discoItems.getItems() == null || discoItems.getItems().isEmpty())
					return new JabberId[0];
				
				JabberId[] resources = new JabberId[discoItems.getItems().size()];
				for (int i = 0; i < discoItems.getItems().size(); i++) {
					Item item = discoItems.getItems().get(i);
					resources[i] = item.getJid();
				}
				
				return resources;
			}
		});
	}

}
