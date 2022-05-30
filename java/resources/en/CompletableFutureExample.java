
/*
 * This example shows how to use CompletableFutures and how to launch
 * them and define a callback to be called when they finish. Also, we
 * can join many CompletableFuture objects to run sequentially
 */

import java.util.concurrent.*;
import java.util.*;
import java.io.*;

// Auxiliary class to transform a string into Person through a CompletableFuture
class Person
{
	private String name;
	private int age;
	
	public Person(String name, int age)
	{
		this.name = name;
		this.age = age;
	}
	
	@Override
	public String toString()
	{
		return name + ", " + age + " years";
	}
}

public class CompletableFutureExample
{
	static Random rnd = new Random();
	
	static CompletableFuture<Integer> doSomething()
	{
		return CompletableFuture.supplyAsync(() -> {
			int random = 500 + rnd.nextInt(5000);
			try
			{
				Thread.sleep(random);
				return random;
			} catch (InterruptedException e) {
				return -1;
			}			
		});
	}
	
	public static void main(String[] args)
	{
		// First, we join many single CompletableFuture objects
		// using "thenApply" if we want to return something else,
		// or "thenAccept" if we just want to get previous result and
		// do something with it.
		CompletableFuture.supplyAsync(() -> {
			try
			{
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException ex) {}
				// Uncomment this line to trigger "exceptionally" clause
				// int n = 4 / 0;
				return "Peter;28";
			}).exceptionally((error) -> {
				// Only if previous step throws an error
				System.err.println("Error: " + error.getMessage());
				return "Error;0";
			}).thenApply((str) -> {
				// Process the string and return a Person
				String[] parts = str.split(";");
				return new Person(parts[0], Integer.parseInt(parts[1]));
			}).thenAccept((person) -> System.out.println(person));
		
		InputStreamReader in = new InputStreamReader(System.in);
		System.out.println("Press enter to exit (let the task finish first)");
		try
		{
			in.read();
		} catch (IOException ex) { }		
		
		// Now, let's launch many CompletableFuture objects and define
		// a callback to be run when the first of them finishes
		
		CompletableFuture allTasks = 
			CompletableFuture.anyOf(doSomething(), doSomething(), doSomething());
		
		allTasks.thenAccept(result -> {
			System.out.println("The first task finishes in " + result + " ms.");
		});
		
		while(!allTasks.isDone())
		{
			try
			{
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException ex) {}
		}		
	}
}
