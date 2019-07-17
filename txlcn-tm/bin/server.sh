SOURCE="$0"
while [ -h "$SOURCE"  ]; do # resolve $SOURCE until the file is no longer a symlink
    DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd  )"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /*  ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
cd "$( cd -P "$( dirname "$SOURCE"  )" && pwd  )"/..

source /etc/profile
APP_HOME=`pwd`
APP_NAME=txlcn-tm
MAIN_CLASS=com.codingapi.txlcn.tm.TMApplication
PID_FILE=$APP_HOME'/bin/program.pid'
LOG_DIR=$APP_HOME/logs/
CLASSPATH=${APP_HOME}'/conf'

JMX_PORT=22039
DEBUG_PORT=22031

JAVA_OPTS=" -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/urandom -Dlog4j2.AsyncQueueFullPolicy=Discard"
JAVA_OPTS+=" -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS+=" -Xmx512m -Xms512m -XX:NewRatio=2 -XX:MaxMetaspaceSize=256m -XX:-UseBiasedLocking -XX:CompileThreshold=20000"
JAVA_OPTS+=" -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly"
JAVA_OPTS+=" -XX:-OmitStackTraceInFastThrow -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled"
JAVA_OPTS+=" -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -Xloggc:${LOG_DIR}/jvm.log"
JAVA_OPTS+=" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR} -XX:ErrorFile=${LOG_DIR}/java_error_%p.log"\
JAVA_OPTS+=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT}"



##########
# Setup JAVA if unset
##################################################
if [ -z "$JAVA" ]; then
  JAVA=$(which java)
fi

if [ -z "$JAVA" ]; then
  echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.5) in your PATH." 2>&2
  exit 1
fi


#-------------------------- class path jar package -----------------------
SEARCH_JAR_PATH=(
        "$APP_HOME/lib"
        )


for jarpath in ${SEARCH_JAR_PATH[@]}; do
        for file in $jarpath/*.jar; do
                # check file is in classpath
                result=$(echo "$CLASSPATH" | grep "$file")
                if [[ "$result" == "" ]]; then
                        CLASSPATH=$CLASSPATH:$file;
                fi
        done
done
#-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n
function start_program(){
	if [ -f $PID_FILE ]; then
      echo "program is running exit."
	  exit 0
    fi
    echo -n "starting program ... "
	  #nohup  $JAVA -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n  -classpath $CLASSPATH $MAIN_CLASS --spider.home=${APP_HOME} >/dev/null 2>&1 &
	  nohup  $JAVA -Djava.security.egd=file:/dev/./urandom -classpath $CLASSPATH $MAIN_CLASS >/dev/null 2>&1 &
    if [ $? -eq 0 ]
    then
      if /bin/echo -n $! > "$PID_FILE"
      then
        #sleep 1
        echo STARTED
      else
        echo FAILED TO WRITE PID
        exit 1
      fi
    else
      echo PROGRAM DID NOT START
      exit 1
    fi
}

#-------------------------------------------------------
function stop_program2(){
	#--------------------------- kill program start --------------------
	echo -n "Stopping program ... "
    if [ ! -f "$PID_FILE" ]
    then
      echo "no the program to stop (could not find file $PID_FILE)"
    else
     	kill  $(cat "$PID_FILE")
     	sleep 3
		pids=`pgrep chrome`
		kill -9 $pids
      rm "$PID_FILE"
      echo STOPPED
    fi
}

#-------------------------------------------------------
function stop_program(){
	#--------------------------- kill program start --------------------
    ps -ef|grep -v grep|grep -v $$|grep -v server.sh|grep -v .out|grep ${APP_NAME}|awk '{print $2}'|xargs kill
    echo -n "Stopping program ."
    for ((  i=0;i<3;i++ ))
    do
        sleep 1
        echo -n "."
    done
    if [ -f "$PID_FILE" ]
    then
      rm "$PID_FILE"
    fi
    echo STOPPED
}






ACTION=$1
case "$ACTION" in
  start)
	start_program
  ;;
  stop)
	stop_program
  ;;
  stop2)
  	stop_program2
  ;;
  restart)
	stop_program
	start_program
  ;; 
  check)
    echo "Checking arguments to $PROJECT_NAME: "
    echo "JAVA_HOME     		=  $JAVA_HOME"
    echo "PROJECT_HOME     	=  $PROJECT_HOME"
    echo "LOG_FILE     		=  $LOG_FILE"
    echo "MAIN_JAR     		=  $MAIN_JAR"
    echo "MAIN_CLASS		=  $MAIN_CLASS"
    echo "JAVA_OPTIONS   		=  ${JAVA_OPTIONS[*]}"
    echo "SEARCH_JAR_PATH	=  ${SEARCH_JAR_PATH[*]}"
    echo "JAVA           		=  $JAVA"
    echo "CLASSES_PATH      	=  $CLASSES_PATH"
    echo

    if [ -f $PID_FILE ];
    then
      echo "RUNNING PID	=$(cat "$PID_FILE")"
      exit 0
    fi
    exit 1

    ;;
  *)
    usage
    ;;
esac
exit 0
