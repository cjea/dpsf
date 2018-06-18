# dpsf

dpsf is an easy way to format docker ps output

## Usage

To make this executable, first compile to standalone jar

```
lein compile
lein uberjar
```

Then, do this crazy stuff I found online:
Create a file stub.sh

```
#!/bin/sh
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
java=java
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi
exec "$java" $java_args -jar $MYSELF "$@"
exit 1
```

Then

```
cat <path to stub.sh> <path to standalone jar> > dpsf && chmod u+x dpsf
```

Use as such:

```
dpsf Names Image CreatedAt
```

```
dpsf default
```
