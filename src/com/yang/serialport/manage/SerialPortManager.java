package com.yang.serialport.manage;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import com.yang.serialport.exception.NoSuchPort;
import com.yang.serialport.exception.NotASerialPort;
import com.yang.serialport.exception.PortInUse;
import com.yang.serialport.exception.ReadDataFromSerialPortFailure;
import com.yang.serialport.exception.SendDataToSerialPortFailure;
import com.yang.serialport.exception.SerialPortInputStreamCloseFailure;
import com.yang.serialport.exception.SerialPortOutputStreamCloseFailure;
import com.yang.serialport.exception.SerialPortParameterFailure;
import com.yang.serialport.exception.TooManyListeners;
import com.yang.serialport.utils.ArrayUtils;

/**
 * 串口管理 serial port management
 * 
 * @author yangle
 */
public class SerialPortManager {

	/**
	 * 查找所有可用端口  Find all available ports
	 *
	 * @return List of available port names 可用端口名称列表
	 */
	public static final ArrayList<String> findPort(){
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();

		ArrayList<String> portNameList = new ArrayList<>();

		while(portList.hasMoreElements()){
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}
		return portNameList;
	}



	/**
	 * 打开串口  Open the corresponding serial port:
	 *
	 * @param portName
	 *            端口名称
	 * @param baudrate
	 *            波特率
	 * @return serial port object  串口对象
	 * @throws SerialPortParameterFailure
	 *             设置串口参数失败
	 * @throws NotASerialPort
	 *             端口指向设备不是串口类型
	 * @throws NoSuchPort
	 *             没有该端口对应的串口设备
	 * @throws PortInUse
	 *             端口已被占用
	 */
	public static final SerialPort openPort(String portName, int baudrate) throws SerialPortParameterFailure, NotASerialPort, NoSuchPort,PortInUse{
		try{
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

		CommPort commPort = portIdentifier.open(portName, 2000);

		if(commPort instanceof SerialPort){
			SerialPort serialPort = (SerialPort) commPort;

			try{
				serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			}catch (UnsupportedCommOperationException e){
				throw new SerialPortParameterFailure();
			}
			return serialPort;
		}
		else{
			throw new NotASerialPort();
		}
		}catch(NoSuchPortException e1){
			throw new NoSuchPort();
		}catch (PortInUseException e2){
			throw new PortInUse();
		}
	}


	/**
	 * 关闭串口 Close the serial port
	 * 
	 *
	 *            待关闭的串口对象
	 */
	public static void closePort(SerialPort serialPort){
		if(serialPort != null){
			serialPort.close();
			serialPort = null;
		}
	}

	/**
	 * 往串口发送数据 Send data to the serial port
	 * 
	 * @param serialPort
	 *            串口对象
	 * @param order
	 *            待发送数据
	 * @throws SendDataToSerialPortFailure
	 *             向串口发送数据失败
	 * @throws SerialPortOutputStreamCloseFailure
	 *             关闭串口对象的输出流出错
	 */
	public static void sendToPort(SerialPort serialPort, byte[] order) throws SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure{
		OutputStream outputStream = null;
		try{
			outputStream = serialPort.getOutputStream();
			outputStream.write(order);
			outputStream.flush();
		}catch (IOException e) {
			throw new SendDataToSerialPortFailure();
		}finally {
			try{
				if(outputStream != null){
					outputStream.close();
					outputStream = null;
				}
			} catch (IOException e) {
				throw new SerialPortOutputStreamCloseFailure();
			}
		}
	}



	/** 
	 * 从串口读取数据 Read data from a serial port
	 *  
	 * @param serialPort 
	 *            当前已建立连接的SerialPort对象 
	 * @return 读取到的数据 data have read
	 */
	public static byte[] readFromPort(SerialPort serialPort) {
		InputStream inputStream = null;
		byte[] bytes = {};
		try{
			inputStream = serialPort.getInputStream();
			byte[] readBuffer = new byte[1];
			int bytesNum = inputStream. read(readBuffer);
			while(bytesNum > 0){
				bytes = ArrayUtils.concat(bytes, readBuffer);
				bytesNum = inputStream.read(readBuffer);
			}
		} catch (IOException e) {
			new ReadDataFromSerialPortFailure().printStackTrace();
		}finally {
			try{
				if(inputStream != null){
					inputStream.close();
					inputStream = null;
				}
			} catch (IOException e) {
				new SerialPortInputStreamCloseFailure().printStackTrace();
			}
		}
		return bytes;
	}


	/**
	 * 添加监听器  Add a listener
	 * 
	 *
	 *            串口对象
	 * @param listener
	 *            串口监听器
	 * @throws TooManyListeners
	 *             监听类对象过多
	 */
	public static void addListener(SerialPort serialPort, SerialPortEventListener listener) throws TooManyListeners{
		try{
			serialPort.addEventListener(listener);
			serialPort.notifyOnDataAvailable(true);
			serialPort.notifyOnBreakInterrupt(true);
		} catch (TooManyListenersException e) {
			throw new TooManyListeners();
		}
	}

}
