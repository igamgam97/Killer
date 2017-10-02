/**
  * Created by gamgam on 10/1/17.
  */
import java.io.File
import java.util.Scanner
import sys.process._


/*
programm find the most inefficient process  and kill him
the procces isn't effective ,if it requirs many memory and few cpu
 */
object Killer extends App{
  /*
  find information about CPU from file  /proc/<PID>/stat and get CPUTimePID=utime+stime+cutime+cstime
  utime-CPU time spent in user code
  stime-CPU time spent in kernel code
  cutime-Waited-for children's CPU time spent in user code
  cstime-Waited-for children's CPY time stent in kernel code
   */
  def findInformationOfCpuFromStat(PID:String): Int = {
    val pathToCpu="/proc/"+PID+"/stat"
    var CPUTimePID = 0
    if (new File(pathToCpu).exists()) {
      val scannerOfCpu = new Scanner(new File(pathToCpu))
      var i = 0
      for(i<-1 to 16) if (i < 13) scannerOfCpu.next() else CPUTimePID += scannerOfCpu.nextInt();
    }
    CPUTimePID
  }
  //find information about Memory(RSS) from file /proc/<PID>/status RSS(22 located on 22 line)
  def findInformationOfMemFromStatus(PID:String): Int = {
    var PIDMemory = 0
    val pathTosmaps = "/proc/" + PID + "/status"
    if ((new File(pathTosmaps).exists()) && (!(new Scanner(new File(pathTosmaps)).nextLine()).isEmpty)) {
      val scannerofMemory = new Scanner(new File(pathTosmaps))
      for (i<-1 to 21) scannerofMemory.nextLine()
      val informationFromSmaps = scannerofMemory.nextLine()
      if (!(informationFromSmaps.contains("Sig"))) {
        val scannerOfMemory = new Scanner(informationFromSmaps)
        scannerOfMemory.next()
        PIDMemory = scannerOfMemory.nextInt()

      }
    }
    PIDMemory
  }

  var PIDOfCondidate = ""
  var memoryOfCondidate = 0
  var cpuOfCondidate = Integer.MAX_VALUE;
  val allCatalogsFromOpt = "ls -a /proc".!! //get all name all file from /proc
  val scanner = new Scanner(allCatalogsFromOpt);
  while (scanner.hasNext()) {
    val PID = scanner.next()
    // test on isPID
    if (PID.matches("^[0-9]+$")) {
      //RSS <PID>
      val PIDMemory=findInformationOfMemFromStatus(PID)
      //CPU(Time) <PID>
      val CPUTimePID = findInformationOfCpuFromStat(PID)
      //find candidate on death
      if ((PIDMemory > memoryOfCondidate) && (CPUTimePID < cpuOfCondidate)) {
        cpuOfCondidate = CPUTimePID
        memoryOfCondidate = PIDMemory
        PIDOfCondidate=PID;
      }

    }

  }
  ("kill "+PIDOfCondidate).! //kill candidate
}