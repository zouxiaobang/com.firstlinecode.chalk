package com.firstlinecode.chalk.demo.lep;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.im.roster.Item;
import com.firstlinecode.basalt.protocol.im.roster.Roster;
import com.firstlinecode.basalt.protocol.im.stanza.Message;
import com.firstlinecode.chalk.core.ErrorException;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.demo.Demo;
import com.firstlinecode.chalk.xeps.muc.events.Invitation;
import com.firstlinecode.chalk.xeps.muc.events.InvitationEvent;
import com.firstlinecode.chalk.xeps.muc.events.RoomEvent;

public class AgilestMobile extends LepClient {

	public AgilestMobile(Demo demo) {
		super(demo, "Agilest/mobile");
	}

	@Override
	public void asked(JabberId user, String message) {
		super.asked(user, message);
		
		if (getRunCount().intValue() == 1) {
			im.getSubscriptionService().refuse(user, "Who are you?");
		} else {
			Roster roster = new Roster();
			Item item = new Item();
			item.setJid(user);
			item.setName("Boss");
			item.getGroups().add("Buddies of FirstLineCode.");
			roster.addOrUpdate(item);
			
			im.getRosterService().add(roster);
			
			demo.startClient(this.getClass(), DonggerOffice.class);
			
			new Thread(new ApproveAndSubscribeThread(user)).start();
		}
	}
	
	private class ApproveAndSubscribeThread implements Runnable {
		private JabberId jid;
		
		public ApproveAndSubscribeThread(JabberId jid) {
			this.jid = jid;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			im.getSubscriptionService().approve(jid);
		}
		
	}

	@Override
	protected void configureStreamConfig(StandardStreamConfig streamConfig) {
		streamConfig.setResource("mobile");
		streamConfig.setTlsPreferred(true);
	}

	@Override
	protected String[] getUserNameAndPassword() {
		return new String[] {"agilest", "a_good_guy"};
	}
	
	@Override
	public void received(RoomEvent<?> event) {
		super.received(event);
		
		if (event instanceof InvitationEvent) {
			Invitation invitation = (Invitation)event.getEventObject();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				joinRoom(invitation);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void received(Message message) {
		super.received(message);
		
		if (!"Are you still online?".equals(message.getBodies().get(0).getText()))
			return;
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		demo.stopClient(this.getClass(), DonggerOffice.class);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		demo.stopClient(this.getClass(), DonggerHome.class);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		demo.stopClient(this.getClass(), AgilestMobile.class);
	}

}
