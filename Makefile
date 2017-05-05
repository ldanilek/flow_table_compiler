run: all
	java Main Optimizations DeadVariableElimination 

all:
	javac *.java

optest: all
	time java Main 10000000
	time java Main 20000000
	time java Main 30000000
	time java Main 40000000
	time java Main 50000000
	time java Main 60000000
	time java Main 70000000
	time java Main 80000000
	time java Main 90000000
	time java Main 100000000

clean:
	rm *.class
