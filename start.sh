## 构建
#  不适用单元测试，加快构建
mvn clean package -Dmaven.test.skip=true

MAINCLASS="qdu.edu.cn.Main"
HOME="$(dirname "$(realpath "$0")")"

CLASSPATH=${HOME}"/target/classes"
JARPATH=${HOME}"/target/"

# 添加 lib 目录中的所有 JAR 文件
for f in ${HOME}/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:${f}
done

# echo $CLASSPATH
# 运行 Java 程序，解决相对目录问题
cd $HOME
# 运行 Java 程序，加载依赖
java -cp "$CLASSPATH" "$MAINCLASS" "$HOME"

# java -jar "$JARPATH""TSBM-1.0.jar"
