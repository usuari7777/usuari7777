/*
 * This example shows how to use Callables instead of Runnables
 * when we want threads to return a value.
 */

import java.util.concurrent.*;
import java.util.*;

public class CallablesExample
{
	public static Callable<Integer> getSumCallable(int num1, int num2, 
		int secondsSleep)
	{
		return () -> {
			try
			{
				TimeUnit.SECONDS.sleep(secondsSleep);
				return num1 + num2;
			} catch (InterruptedException e) {
				throw new IllegalStateException("Task interrupted", e);
			}
		};
	}	
	
	public static void main(String[] args)
	{
		// First of all, we create just one Callable and launch it
		
		ThreadPoolExecutor executor 
			= (ThreadPoolExecutor)Executors.newFixedThreadPool(1);
			
		Future<Integer> future = executor.submit(getSumCallable(2, 3, 5));
		
		executor.shutdown();
		
		System.out.println("Using 1 callable...");		
		System.out.println("future done? " + future.isDone());
		try
		{
			Integer result = future.get(); // It BLOCKS main thread until it returns!
			System.out.println("future done? " + future.isDone());
			System.out.println("Result: " + result);
		} catch (InterruptedException ex) {
		} catch (ExecutionException ex) { }
		
		// Now, we launch a set of callables and wait for all of
		// them to finish
		
		List<Callable<Integer>> callables = Arrays.asList(
			getSumCallable(3, 6, 2),
			getSumCallable(5, 8, 3),
			getSumCallable(12, 3, 5)
		);

		executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(3);
		List<Future<Integer>> futures = new ArrayList<>();

		try
		{
			// This blocks current thread
			// futures = executor.invokeAll(callables);
			
			// We can use this structure instead if we don't want to block it
			for (Callable c: callables)
				futures.add(executor.submit(c));
			
			System.out.println("Many callables launched...");
			
			executor.shutdown();
			while (!executor.isTerminated())
			{
				System.out.println("Waiting...");
				Thread.sleep(500);
			}
			
			futures.forEach(fut -> {
				try
				{
					System.out.println(fut.get());
				} catch (InterruptedException | ExecutionException e) {
					throw new IllegalStateException(e);
				}
			});
		} catch (InterruptedException ex) {}
	}
} 
