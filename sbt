#!/bin/sh

dir=$(dirname $0)
cd "$dir"

url="http://scalasbt.artifactoryonline.com/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.12.2/sbt.zip"

sbt="sbt-launch-0.12.2.jar"

# set the right tool to download sbt
if [ -n "$tool" ]; then
    echo -n
elif [ -n "$(which wget)" ]; then
    tool="wget"
elif [ -n "$(which curl)" ]; then
    tool="curl"
else
    echo "Couldn't find a tool to download sbt. Please do the following"
    echo "* download $url"
    echo "* set the name of the file to $sbt"
    echo "* relaunch ./sbt"
    exit 1
fi

# download the sbt launcher if it's not already here
if [ ! -f "$sbt" ]; then
    case "$tool" in
        "wget"*)
            wget "$url" -O ./sbt.zip
            unzip -p sbt.zip sbt/bin/sbt-launch.jar > "./$sbt"
            rm -f ./sbt.zip
            ;;
        "curl"*)
            curl "$url" -o ./sbt.zip
            unzip -p sbt.zip sbt/bin/sbt-launch.jar > "./$sbt"
            rm -f ./sbt.zip
            ;;
        *)
            echo "don't know this tool: $tool"
            exit 2
    esac
fi

# tweak this line according to your needs
java $SBT_PROPS -Xmx512M -jar -Dfile.encoding=UTF8 -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m "$dir/$sbt" "$@"

