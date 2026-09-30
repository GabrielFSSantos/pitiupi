JAR = ../pitiupi/dist/pitiupi.jar
OUT_DIR = /out
META-INF_DIR = src/META-INF
DIST = dist/mpChat.jar

SOURCES = $(shell find -name "*.java")

all:
	mkdir -p $(OUT_DIR)
	javac -cp "$(JAR)" -d "$(OUT_DIR)" $(SOURCES)
	cp -r "$(META-INF_DIR)" "$(OUT_DIR)"
	jar cf "$(DIST)" -C "$(OUT_DIR)" .

clean:
	rm -rf "$(OUT_DIR)"/*