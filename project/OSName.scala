package cn.gov.heshan.sbt

object OSName {

  val OS = System.getProperty("os.name").toLowerCase

  def isLinux = OS.indexOf("linux") >= 0

  def isMacOS = OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") < 0

  def isMacOSX = OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") > 0

  def isWindows = OS.indexOf("windows") >= 0

  def isOS2 = OS.indexOf("os/2") >= 0

  def isSolaris = OS.indexOf("solaris") >= 0

  def isSunOS = OS.indexOf("sunos") >= 0

  def isMPEiX = OS.indexOf("mpe/ix") >= 0

  def isHPUX = OS.indexOf("hp-ux") >= 0

  def isAix = OS.indexOf("aix") >= 0

  def isOS390 = OS.indexOf("os/390") >= 0

  def isFreeBSD = OS.indexOf("freebsd") >= 0

  def isIrix = OS.indexOf("irix") >= 0

  def isDigitalUnix = OS.indexOf("digital") >= 0 && OS.indexOf("unix") > 0

  def isNetWare = OS.indexOf("netware") >= 0

  def isOSF1 = OS.indexOf("osf1") >= 0

  def isOpenVMS = OS.indexOf("openvms") >= 0

}