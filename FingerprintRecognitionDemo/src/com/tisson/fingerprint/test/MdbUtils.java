/**
 * 
 */
package com.tisson.fingerprint.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.regaltec.ccatstep.common.RtsBase64;

/**
 * @author yihaijun
 * 
 */
public class MdbUtils {
	private static SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss:SSS");

	/**
	 * TODO : ��ȡ�ļ�access
	 * 
	 * @param filePath
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static void readFileACCESS(File mdbFile, String sql) {
		Properties prop = new Properties();
		prop.put("charSet", "gb2312"); // �����ǽ����������
		prop.put("user", "");
		prop.put("password", "");
		String url = "jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};DBQ="
				+ mdbFile.getAbsolutePath();
		Statement stmt = null;
		ResultSet rs = null;
		String tableName = null;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			// ���ӵ�mdb�ļ�
			Connection conn = DriverManager.getConnection(url, prop);
			ResultSet tables = conn.getMetaData().getTables(
					mdbFile.getAbsolutePath(), null, null,
					new String[] { "TABLE" });
			// ��ȡ��һ������
			if (tables.next()) {
				tableName = tables.getString(3);// getXXX can only be used once
			} else {
				return;
			}
			stmt = (Statement) conn.createStatement();
			// ��ȡ��һ���������
			rs = stmt.executeQuery(sql);
			ResultSetMetaData data = rs.getMetaData();
			printColumnInfo(data);
			while (rs.next()) {
				String vFingerId = rs.getString("vFingerId");
				byte[] fpData = rs.getBytes("vFingerDat");

				try {
					String fpDataBase64 = RtsBase64.encodeBytes(fpData);
					String txtFilePath = "D:\\Program Files (x86)\\MjSystem5.1\\Database\\ChineseSimple\\fingerprint-"+vFingerId+"-ISO-19794-2-base64.txt";
					File txtFile = new File(txtFilePath);
					if (txtFile.exists()) {
						txtFile.delete();
					}
					txtFile.createNewFile();
					FileOutputStream fs = new FileOutputStream(txtFile, true); // �ڸ��ļ���ĩβ�������

					fs.write(fpDataBase64.getBytes());

					fs.close();
				} catch (Throwable t) {
					System.out.println("[" + df.format(new Date()) + "] "
							+ t.toString());
				}

			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static void printColumnInfo(ResultSetMetaData data) {
		try {
			for (int i = 1; i <= data.getColumnCount(); i++) {
				// ��������е���Ŀ��ʵ������
				int columnCount2 = data.getColumnCount();
				// ���ָ���е�����
				String columnName = data.getColumnName(i);
				// ���ָ���е���ֵ
				int columnType = data.getColumnType(i);
				// ���ָ���е�����������
				String columnTypeName = data.getColumnTypeName(i);
				// ���ڵ�Catalog����
				String catalogName = data.getCatalogName(i);
				// ��Ӧ�������͵���
				String columnClassName = data.getColumnClassName(i);
				// �����ݿ������͵�����ַ�����
				int columnDisplaySize = data.getColumnDisplaySize(i);
				// Ĭ�ϵ��еı���
				String columnLabel = data.getColumnLabel(i);
				// ����е�ģʽ
				String schemaName = data.getSchemaName(i);
				// ĳ�����͵ľ�ȷ��(���͵ĳ���)
				int precision = data.getPrecision(i);
				// С������λ��
				int scale = data.getScale(i);
				// ��ȡĳ�ж�Ӧ�ı���
				String tableName2 = data.getTableName(i);
				// �Ƿ��Զ�����
				boolean isAutoInctement = data.isAutoIncrement(i);
				// �����ݿ����Ƿ�Ϊ������
				boolean isCurrency = data.isCurrency(i);
				// �Ƿ�Ϊ��
				int isNullable = data.isNullable(i);
				// �Ƿ�Ϊֻ��
				boolean isReadOnly = data.isReadOnly(i);
				// �ܷ������where��
				boolean isSearchable = data.isSearchable(i);
				System.out.println("[" + df.format(new Date())
						+ " data.getColumnCount()=" + data.getColumnCount());
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "���ֶ�����:" + columnName);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "������,����SqlType�еı��:" + columnType);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "������������:" + columnTypeName);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "���ڵ�Catalog����:" + catalogName);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "��Ӧ�������͵���:" + columnClassName);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "�����ݿ������͵�����ַ�����:" + columnDisplaySize);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "��Ĭ�ϵ��еı���:" + columnLabel);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "��ģʽ:" + schemaName);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "���͵ľ�ȷ��(���͵ĳ���):" + precision);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "С������λ��:" + scale);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "��Ӧ�ı���:" + data.getTableName(1));
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "�Ƿ��Զ�����:" + isAutoInctement);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "�����ݿ����Ƿ�Ϊ������:" + isCurrency);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "�Ƿ�Ϊ��:" + isNullable);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "�Ƿ�Ϊֻ��:" + isReadOnly);
				System.out.println("[" + df.format(new Date()) + " �����" + i
						+ "�ܷ������where��:" + isSearchable);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight,
			String path) throws IOException {
		java.io.FileOutputStream fos = new java.io.FileOutputStream(path);
		java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);

		int w = (((nWidth + 3) / 4) * 4);
		int bfType = 0x424d; // λͼ�ļ����ͣ�0��1�ֽڣ�
		int bfSize = 54 + 1024 + w * nHeight;// bmp�ļ��Ĵ�С��2��5�ֽڣ�
		int bfReserved1 = 0;// λͼ�ļ������֣�����Ϊ0��6-7�ֽڣ�
		int bfReserved2 = 0;// λͼ�ļ������֣�����Ϊ0��8-9�ֽڣ�
		int bfOffBits = 54 + 1024;// �ļ�ͷ��ʼ��λͼʵ������֮����ֽڵ�ƫ������10-13�ֽڣ�

		dos.writeShort(bfType); // ����λͼ�ļ�����'BM'
		dos.write(changeByte(bfSize), 0, 4); // ����λͼ�ļ���С
		dos.write(changeByte(bfReserved1), 0, 2);// ����λͼ�ļ�������
		dos.write(changeByte(bfReserved2), 0, 2);// ����λͼ�ļ�������
		dos.write(changeByte(bfOffBits), 0, 4);// ����λͼ�ļ�ƫ����

		int biSize = 40;// ��Ϣͷ������ֽ�����14-17�ֽڣ�
		int biWidth = nWidth;// λͼ�Ŀ�18-21�ֽڣ�
		int biHeight = nHeight;// λͼ�ĸߣ�22-25�ֽڣ�
		int biPlanes = 1; // Ŀ���豸�ļ��𣬱�����1��26-27�ֽڣ�
		int biBitcount = 8;// ÿ�����������λ����28-29�ֽڣ���������1λ��˫ɫ����4λ��16ɫ����8λ��256ɫ������24λ�����ɫ��֮һ��
		int biCompression = 0;// λͼѹ�����ͣ�������0����ѹ������30-33�ֽڣ���1��BI_RLEBѹ�����ͣ���2��BI_RLE4ѹ�����ͣ�֮һ��
		int biSizeImage = w * nHeight;// ʵ��λͼͼ��Ĵ�С��������ʵ�ʻ��Ƶ�ͼ���С��34-37�ֽڣ�
		int biXPelsPerMeter = 0;// λͼˮƽ�ֱ��ʣ�ÿ����������38-41�ֽڣ��������ϵͳĬ��ֵ
		int biYPelsPerMeter = 0;// λͼ��ֱ�ֱ��ʣ�ÿ����������42-45�ֽڣ��������ϵͳĬ��ֵ
		int biClrUsed = 0;// λͼʵ��ʹ�õ���ɫ���е���ɫ����46-49�ֽڣ������Ϊ0�Ļ���˵��ȫ��ʹ����
		int biClrImportant = 0;// λͼ��ʾ��������Ҫ����ɫ��(50-53�ֽ�)�����Ϊ0�Ļ���˵��ȫ����Ҫ

		dos.write(changeByte(biSize), 0, 4);// ������Ϣͷ���ݵ����ֽ���
		dos.write(changeByte(biWidth), 0, 4);// ����λͼ�Ŀ�
		dos.write(changeByte(biHeight), 0, 4);// ����λͼ�ĸ�
		dos.write(changeByte(biPlanes), 0, 2);// ����λͼ��Ŀ���豸����
		dos.write(changeByte(biBitcount), 0, 2);// ����ÿ������ռ�ݵ��ֽ���
		dos.write(changeByte(biCompression), 0, 4);// ����λͼ��ѹ������
		dos.write(changeByte(biSizeImage), 0, 4);// ����λͼ��ʵ�ʴ�С
		dos.write(changeByte(biXPelsPerMeter), 0, 4);// ����λͼ��ˮƽ�ֱ���
		dos.write(changeByte(biYPelsPerMeter), 0, 4);// ����λͼ�Ĵ�ֱ�ֱ���
		dos.write(changeByte(biClrUsed), 0, 4);// ����λͼʹ�õ�����ɫ��
		dos.write(changeByte(biClrImportant), 0, 4);// ����λͼʹ�ù�������Ҫ����ɫ��

		for (int i = 0; i < 256; i++) {
			dos.writeByte(i);
			dos.writeByte(i);
			dos.writeByte(i);
			dos.writeByte(0);
		}

		byte[] filter = null;
		if (w > nWidth) {
			filter = new byte[w - nWidth];
		}

		for (int i = 0; i < nHeight; i++) {
			dos.write(imageBuf, (nHeight - 1 - i) * nWidth, nWidth);
			if (w > nWidth)
				dos.write(filter, 0, w - nWidth);
		}
		dos.flush();
		dos.close();
		fos.close();
	}

	private static byte[] changeByte(int data) {
		return intToByteArray(data);
	}

	private static byte[] intToByteArray(final int number) {
		byte[] abyte = new byte[4];
		// "&" �루AND�������������Ͳ������ж�Ӧλִ�в�������������λ��Ϊ1ʱ���1������0��
		abyte[0] = (byte) (0xff & number);
		// ">>"����λ����Ϊ�������λ��0����Ϊ�������λ��1
		abyte[1] = (byte) ((0xff00 & number) >> 8);
		abyte[2] = (byte) ((0xff0000 & number) >> 16);
		abyte[3] = (byte) ((0xff000000 & number) >> 24);
		return abyte;
	}

	public static void main(String[] args) {
		readFileACCESS(
				new File(
						"D:\\Program Files (x86)\\MjSystem5.1\\Database\\ChineseSimple\\SystemData_MJ.mdb"),
				"Select vFingerId,vFingerDat From FP_FingerData");
	}
}
