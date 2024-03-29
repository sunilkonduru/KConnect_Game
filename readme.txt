You may run any number of AIs against one another. The tournament runs each AI against every other in round robin style. For this reason, it can take a long time to run especially when a large number of AIs are in the tournament. The tournament is written in java but will work with any AIs written C++ or any combination of java and C++ AIs. 
The tournament must be launched from the command line. To do so start with: 
java -jar Tournament.jar 
then list the names (separated by spaces) of the AIs you would like in the tournament. For example: 
java -jar Tournament.jar MyJavaAI.class MycppAI.exe MyJavaAI2.class

The above command will begin a tournament with three AIs: two were written in java and one in C++. The order of the arguments does not matter. If you are including any java AIs, be sure that the AI's file ends with .class (this should be the case anyway).
For C++ AIs, the file extension does not matter as long as it is NOT .class. The Tournament checks all the arguments and assumes all those that do not end in .class are C++ AIs and will run them accordingly. 

Be aware that when running the tournament, if using C++ AIs, there will be subprocesses spawned by java. This should not cause any problems as long as java permission to spawn processes (it usually does by default). Furthermore, if you decide to cancel the tournament early, be sure to close the tournament by closing out of the GUI. This will make sure that the tournament exits gracefully and that all subprocesses are killed. 

If you decide to kill the tournament process, subprocesses may remain. These will have to be manually killed. They will likely enter an infinite loop when the tournament is killed so they will put a heavy demand on the CPU which should make them easy to track down. There should not be more than two subprocesses running at any given time. 