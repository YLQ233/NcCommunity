//package com.nc.nccommunity;
//
//import java.util.Random;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//
//public class blockingQTest {
//	public static void main(String[] args) {
//		BlockingQueue blockingQueue = new ArrayBlockingQueue(10);
//		new Thread((new Producer(blockingQueue))).start();
//		new Thread((new Customer(blockingQueue))).start();
//		new Thread((new Customer(blockingQueue))).start();
//		new Thread((new Customer(blockingQueue))).start();
//	}
//}
//
//
//class Producer implements Runnable{
//	private BlockingQueue blockingQueue;
//	public Producer(BlockingQueue blockingQueue){
//		this.blockingQueue = blockingQueue;
//	}
//
//	@Override
//	public void run() {
//		try {
//			for(int i=0;i<100;i++) {
//				Thread.sleep(5);
//				blockingQueue.put(i);
//				System.out.println(Thread.currentThread().getId()+"shengchan"+ blockingQueue.size());
//			}
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//	}
//}
//
//class Customer implements Runnable{
//	private BlockingQueue blockingQueue;
//	public Customer(BlockingQueue blockingQueue){
//		this.blockingQueue = blockingQueue;
//	}
//
//	@Override
//	public void run() {
//		try {
//			while(true) {
//				Thread.sleep(new Random().nextInt(100));
//				blockingQueue.take();
//				System.out.println(Thread.currentThread().getId()+"xiaofei"+ blockingQueue.size());
//			}
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//	}
//}