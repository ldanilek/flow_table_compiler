run: all
	java Main Optimizations DeadVariableElimination

all:
	javac *.java

clean:
	rm *.class
