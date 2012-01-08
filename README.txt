stmPlayground - simple HelloWorld-type tryouts using AKKA and java. Namely trying to run concurrent operations wrapped up in transactions ( STM ).

I tried to simulate somehow what often happens on Amazon ( for example ), when they show they have 3 items of some type on stock and 6 users concurrently attempt to 
buy the item and .. others :) .

In order to see some results : 
- get the sources
- go with a command line under the root of the project and try :

		- gradle clean
		- gradle build    *** downloads the dependencies and builds the project.
		- gradle eclipse *** if you're using eclipse
		- gradle test      *** run the tests