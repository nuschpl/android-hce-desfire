package net.jpeelaer.hce.desfire;


import org.kevinvalk.hce.framework.Iso7816;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Key;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Util {


	//Authenticated
	public final static byte NO_KEY_AUTHENTICATED=-1;
	
	//Status Word
	public final static short OPERATION_OK=(short)0x9100;
	public final static short NO_CHANGES=(short)0x910C;
	public final static short OUT_OF_EEPROM_ERROR=(short)0x910E;
	public final static short ILLEGAL_COMMAND_CODE=(short)0x911C;
	public final static short INTEGRITY_ERROR=(short)0x911E;
	public final static short NO_SUCH_KEY=(short)0x9140;
	public final static short LENGTH_ERROR=(short)0x917E;
	public final static short PERMISSION_DENIED=(short)0x919D;
	public final static short PARAMETER_ERROR=(short)0x919E;
	public final static short APPLICATION_NOT_FOUND=(short)0x91A0;
	public final static short APPL_INTEGRITY_ERROR=(short)0x91A1;
	public final static short AUTHENTICATION_ERROR=(short)0x91AE;
	public final static short ADDITIONAL_FRAME=(short)0x91AF;
	public final static short BOUNDARY_ERROR=(short)0x91BE;
	public final static short PICC_INTEGRITY_ERROR=(short)0x91C1;
	public final static short COMMAND_ABORTED=(short)0X91CA;
	public final static short PICC_DISABLED_ERROR=(short)0x91CD;
	public final static short COUNT_ERROR=(short)0x91CE;
	public final static short DUPLICATE_ERROR=(short)0x91DE;
	public final static short EEPROM_ERROR=(short)0x91EE;
	public final static short FILE_NOT_FOUND=(short)0x91F0;
	public final static short FILE_INTEGRITY_ERROR=(short)0x91F1;
	
	
	
	public final static byte[] masterFileAID = {(byte)0x00,(byte)0x00,(byte)0x00};
	
	 // crypto operations
	 final static byte TDES = (byte) 0x00;
	 final static byte TKTDES = (byte) 0x40;
	 final static byte AES = (byte) 0x80;

	 //File types
	 final static byte STANDARD_DATA_FILE=(byte)0x0;
	 final static byte BACKUP_DATA_FILE=(byte)0X01;
	 final static byte VALUE_FILE=(byte)0x02;
	 final static byte LINEAR_RECORD_FILE=(byte)0x03;
	 final static byte CYCLIC_RECORD_FILE=(byte)0x04;
	 
	 //Transmission modes
	 final static byte PLAIN_COMMUNICATION=(byte)0x00;
	 final static byte PLAIN_COMMUNICATION_MAC=(byte)0x01;
	 final static byte FULLY_ENCRYPTED=(byte)0x02;

	 // 3DES;	 public final static byte[] RANDOM_A={(byte)0xBB,(byte)0xCC,(byte)0xBB,(byte)0xCC,(byte)0xBB,(byte)0xCC,(byte)0xBB,(byte)0xCC};
	 public final static byte[] CHECKSUM_IV={(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};
	 
	 //New Errors
	 public final static short WRONG_VALUE_ERROR=(short)0x916E;
	 
	 //Others
	 public final static byte MAX_DATA_SIZE=100;//MEJORAR ESTE VALOR

	public static byte[] rotateLeft(byte[] c){
		 byte[] c1=new byte[c.length];
		 c1[(byte)(c.length-1)]=c[0];
		 for (byte i = 1; i < c1.length; i++) {
			 c1[(byte)(i-1)]=c[i];
		 }
		 return c1;
	 }
	 public static byte[] rotateRight(byte[] c){
		 byte[] c1=new byte[c.length];
		 c1[0]=c[(byte)(c.length-1)];
		 for (byte i = 1; i < c1.length; i++) {
			 c1[i]=c[(byte)(i-1)];
		 }
		 return c1;
	 }
	
	 
	 public static final byte[] shortToByteArray(final short value) {
			return new byte[] { (byte) (value >>> 8), (byte) (value) };
		}
	public static final short byteArrayToShort(final byte[] b) {
		return (short) (((b[0] & 0xFF) << 8) + (b[1] & 0xFF));
	}
	public static final short valueByteArrayToShort(final byte[] b) {
		return (short) (((b[2] & 0xFF) << 8) + (b[3] & 0xFF));
	}
	public static final byte[] concatByteArray(byte[] a,byte[]b){
		byte[] result=new byte[(short)(a.length+b.length)];
		for (short i = 0; i < a.length; i++) {
			result[i]=a[i];
		}
		for (short i = 0; i < b.length; i++) {
			result[(short)(i+a.length)]=b[i];
		}
		return result;
	}
//	
//	public static byte[] cutByteArray(byte[] a,short offset,short length){
//		byte[] result=new byte[length];
//		for (short i = 0; i < result.length; i++) {
//			result[i]=a[(short)(offset+i)];
//		}
//		return result;
//	}
	/**
	 * Makes a new array with a length multiple of 8 padding with 0
	 * 
	 */
	public static byte[] preparePaddedByteArray(byte[] a){
		
		if((short)(a.length%8)!=(short)0){
			byte[] result=new byte[(short)(a.length+(8-a.length%8))];
			for (short i = 0; i < (short)a.length; i++) {
				result[i]=a[i];
			}
			result[a.length]=(byte)0x80;
			return result;
		}
		else return a;
		
	}
	
	/**
	 * Removes the padding to go back to the original data
	 */
	public static byte[] removePadding(byte[]a){
		byte aux=(byte)0x00;
		byte i=(byte)(a.length);
		while(aux==(byte)0x00){
			i--;
			aux=a[i];
		}
		if(a[i]!=(byte)0x80) return a;//There was no padding
		return Util.subByteArray(a,(byte) 0,(byte)(i-1));
	}
	
	/**
	 * Copy a byte array over the bytes of another byte array
	 * @param input
	 * @param offsetInput
	 * @param length
	 * @param offsetOutput
	 * @return Output
	 */
	public static byte[] copyByteArray( byte[] input, short offsetInput, short length,byte[] output, short offsetOutput){
//		if((byte)(length+offsetInput)>input.length)ISOException.throwIt((short)0xCCAA);
		for (short i = offsetOutput; i < (short)(length+offsetOutput); i++) {
			output[i]=input[(short)(i+offsetInput-offsetOutput)];
		}
		
		return output;
	}

	public static Key createSessionKey(byte[] a,byte[] b, byte keyType) {
		byte[] result = new byte[16];
		String algorithm = "DES";
		if (keyType == Util.TKTDES) {
			algorithm = "DESede";
			result[0]=a[0];
			result[1]=a[1];
			result[2]=a[2];
			result[3]=a[3];
			result[4]=b[0];
			result[5]=b[1];
			result[6]=b[2];
			result[7]=b[3];
			result[8]=a[4];
			result[9]=a[5];
			result[10]=a[6];
			result[11]=a[7];
			result[12]=b[4];
			result[13]=b[5];
			result[14]=b[6];
			result[15]=b[7];
		} else if (keyType == Util.AES) {
			algorithm = "AES";
			// eerste 4 v byte a en b
			for (int i = 0; i < 4; i++) {
				result[i] = a[i];
			}
			for (int i = 4; i < 8; i++) {
				result[i] = b[i-4];
			}
			// laaste 4 bytes van a en b
			for (int i = 8; i < 12; i++) {
				result[i] = a[i+4];
			}
			for (int i = 12; i < 16; i++) {
				result[i] = b[i];
			}
		}
		return new SecretKeySpec(result, algorithm);
	}


	public static byte[] switchBytes(byte[] a) {
		byte[] result=new byte[a.length];
		for (byte i = 0; i < result.length; i++) {
			result[i]=a[(byte)(result.length-i-1)];
		}
		return result;
	}
	
	/**
	 * Takes a part of the byte array
	 * 
	 * @param 	input
	 * @param 	inputInit
	 * 			Index of the first byte copied to the subarray
	 * @param 	inputEnd
	 * 			Index of the last byte copied to the subarray
	 * @return
	 */
	public static byte[] subByteArray(byte[]input,short inputInit,short inputEnd){
		byte[] result=new byte[(byte)(inputEnd-inputInit+1)];
		for (short i = inputInit; i <= inputEnd; i++) {
			result[(short)(i-inputInit)]=input[i];
		}
		return result;
	}

	public static byte[] getCData(byte[] input) {
		byte[] result = new byte[input[Iso7816.OFFSET_CDATA]];
		ByteBuffer.allocate(input.length).put(input).get(result, Iso7816.OFFSET_CDATA, input[Iso7816.OFFSET_CDATA]);
		return result;
	}

	public static short max(short a, short b) {
		if(a>b)return a;
		if(a<b)return b;
		return a;
	}
	
	public static byte[] crc16(byte[]data){
		short crc = 0x0000;
		short[] table = {
		            (short) 0x0000, (short) 0xC0C1, (short) 0xC181, (short) 0x0140, (short) 0xC301, (short) 0x03C0, (short) 0x0280, (short) 0xC241,
		            (short) 0xC601, (short) 0x06C0, (short) 0x0780, (short) 0xC741, (short) 0x0500, (short) 0xC5C1, (short) 0xC481, (short) 0x0440,
		            (short) 0xCC01, (short) 0x0CC0, (short) 0x0D80, (short) 0xCD41, (short) 0x0F00, (short) 0xCFC1, (short) 0xCE81, (short) 0x0E40,
		            (short) 0x0A00, (short) 0xCAC1, (short) 0xCB81, (short) 0x0B40, (short) 0xC901, (short) 0x09C0, (short) 0x0880, (short) 0xC841,
		            (short) 0xD801, (short) 0x18C0, (short) 0x1980, (short) 0xD941, (short) 0x1B00, (short) 0xDBC1, (short) 0xDA81, (short) 0x1A40,
		            (short) 0x1E00, (short) 0xDEC1, (short) 0xDF81, (short) 0x1F40, (short) 0xDD01, (short) 0x1DC0, (short) 0x1C80, (short) 0xDC41,
		            (short) 0x1400, (short) 0xD4C1, (short) 0xD581, (short) 0x1540, (short) 0xD701, (short) 0x17C0, (short) 0x1680, (short) 0xD641,
		            (short) 0xD201, (short) 0x12C0, (short) 0x1380, (short) 0xD341, (short) 0x1100, (short) 0xD1C1, (short) 0xD081, (short) 0x1040,
		            (short) 0xF001, (short) 0x30C0, (short) 0x3180, (short) 0xF141, (short) 0x3300, (short) 0xF3C1, (short) 0xF281, (short) 0x3240,
		            (short) 0x3600, (short) 0xF6C1, (short) 0xF781, (short) 0x3740, (short) 0xF501, (short) 0x35C0, (short) 0x3480, (short) 0xF441,
		            (short) 0x3C00, (short) 0xFCC1, (short) 0xFD81, (short) 0x3D40, (short) 0xFF01, (short) 0x3FC0, (short) 0x3E80, (short) 0xFE41,
		            (short) 0xFA01, (short) 0x3AC0, (short) 0x3B80, (short) 0xFB41, (short) 0x3900, (short) 0xF9C1, (short) 0xF881, (short) 0x3840,
		            (short) 0x2800, (short) 0xE8C1, (short) 0xE981, (short) 0x2940, (short) 0xEB01, (short) 0x2BC0, (short) 0x2A80, (short) 0xEA41,
		            (short) 0xEE01, (short) 0x2EC0, (short) 0x2F80, (short) 0xEF41, (short) 0x2D00, (short) 0xEDC1, (short) 0xEC81, (short) 0x2C40,
		            (short) 0xE401, (short) 0x24C0, (short) 0x2580, (short) 0xE541, (short) 0x2700, (short) 0xE7C1, (short) 0xE681, (short) 0x2640,
		            (short) 0x2200, (short) 0xE2C1, (short) 0xE381, (short) 0x2340, (short) 0xE101, (short) 0x21C0, (short) 0x2080, (short) 0xE041,
		            (short) 0xA001, (short) 0x60C0, (short) 0x6180, (short) 0xA141, (short) 0x6300, (short) 0xA3C1, (short) 0xA281, (short) 0x6240,
		            (short) 0x6600, (short) 0xA6C1, (short) 0xA781, (short) 0x6740, (short) 0xA501, (short) 0x65C0, (short) 0x6480, (short) 0xA441,
		            (short) 0x6C00, (short) 0xACC1, (short) 0xAD81, (short) 0x6D40, (short) 0xAF01, (short) 0x6FC0, (short) 0x6E80, (short) 0xAE41,
		            (short) 0xAA01, (short) 0x6AC0, (short) 0x6B80, (short) 0xAB41, (short) 0x6900, (short) 0xA9C1, (short) 0xA881, (short) 0x6840,
		            (short) 0x7800, (short) 0xB8C1, (short) 0xB981, (short) 0x7940, (short) 0xBB01, (short) 0x7BC0, (short) 0x7A80, (short) 0xBA41,
		            (short) 0xBE01, (short) 0x7EC0, (short) 0x7F80, (short) 0xBF41, (short) 0x7D00, (short) 0xBDC1, (short) 0xBC81, (short) 0x7C40,
		            (short) 0xB401, (short) 0x74C0, (short) 0x7580, (short) 0xB541, (short) 0x7700, (short) 0xB7C1, (short) 0xB681, (short) 0x7640,
		            (short) 0x7200, (short) 0xB2C1, (short) 0xB381, (short) 0x7340, (short) 0xB101, (short) 0x71C0, (short) 0x7080, (short) 0xB041,
		            (short) 0x5000, (short) 0x90C1, (short) 0x9181, (short) 0x5140, (short) 0x9301, (short) 0x53C0, (short) 0x5280, (short) 0x9241,
		            (short) 0x9601, (short) 0x56C0, (short) 0x5780, (short) 0x9741, (short) 0x5500, (short) 0x95C1, (short) 0x9481, (short) 0x5440,
		            (short) 0x9C01, (short) 0x5CC0, (short) 0x5D80, (short) 0x9D41, (short) 0x5F00, (short) 0x9FC1, (short) 0x9E81, (short) 0x5E40,
		            (short) 0x5A00, (short) 0x9AC1, (short) 0x9B81, (short) 0x5B40, (short) 0x9901, (short) 0x59C0, (short) 0x5880, (short) 0x9841,
		            (short) 0x8801, (short) 0x48C0, (short) 0x4980, (short) 0x8941, (short) 0x4B00, (short) 0x8BC1, (short) 0x8A81, (short) 0x4A40,
		            (short) 0x4E00, (short) 0x8EC1, (short) 0x8F81, (short) 0x4F40, (short) 0x8D01, (short) 0x4DC0, (short) 0x4C80, (short) 0x8C41,
		            (short) 0x4400, (short) 0x84C1, (short) 0x8581, (short) 0x4540, (short) 0x8701, (short) 0x47C0, (short) 0x4680, (short) 0x8641,
		            (short) 0x8201, (short) 0x42C0, (short) 0x4380, (short) 0x8341, (short) 0x4100, (short) 0x81C1, (short) 0x8081, (short) 0x4040,
		        };
        for (short i = 0; i < data.length; i++) {
            crc = (short) ((crc >>> 8) ^ table[(crc ^ data[i]) & (short) 0xff]);
        }
        return shortToByteArray(crc);
	}

	public static byte[] crc32(byte[] data) {
		CRC32 x = new CRC32();
		x.update(data);
		int result = (int) ~x.getValue() & 0xFFFFFFFF;
		return ByteBuffer.allocate(4).putInt((int) result).array();
	}

	public static boolean byteArrayCompare(byte[]a,byte[] b){
		if(a.length!=b.length)return false;
		for (byte i = 0; i < a.length; i++) {
			if(a[i]!=b[i])return false;
		}
		return true;
	}
	
	public static byte[] getZeroArray(short length){
		byte[] zeroArray=new byte[length];
		for (short i = 0; i < zeroArray.length; i++) {
			zeroArray[i]=0;	
		}
		return zeroArray;
	}
	
	public static byte[] xorByteArray(byte[]a,byte[]b){
		byte[] result=new byte[a.length];
		for (byte i = 0; i < a.length; i++) {
			result[i]=(byte)(a[i]^b[i]);
		}
		return result;
	}
}
