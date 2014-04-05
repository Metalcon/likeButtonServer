package de.metalcon.like.server.zmqserver;

import net.hh.request_dispatcher.server.ZmqWorker;

import org.zeromq.ZMQ;

import de.metalcon.api.responses.Response;
import de.metalcon.exceptions.MetalconException;
import de.metalcon.like.api.requests.LikeServerRequest;
import de.metalcon.like.server.api.LikeService;

public class LikeButtonServer extends Thread {
	private final static String LISTEN_URI = "tcp://*:1003";

	private final static String STORAGE_DIR = "/dev/shm/likeDB";

	private final LikeService service;
	private final ZmqWorker<LikeServerRequest, Response> worker;

	LikeButtonServer() throws MetalconException {
		service = new LikeService(STORAGE_DIR);

		ZMQ.Context ctx = ZMQ.context(1);

		worker = new ZmqWorker<LikeServerRequest, Response>(ctx, LISTEN_URI,
				new LikeServerRequestHandler(service));
	}

	@Override
	public void start() {
		worker.start();
		super.start();
	}

	@Override
	public void run() {
		while (true) {
			long ns = service.updateAllNodes();
			if (ns < 1E9) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		LikeButtonServer server;
		try {
			server = new LikeButtonServer();
			server.start();
		} catch (MetalconException e) {
			e.printStackTrace();
		}
	}
}
