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
	 * TODO : 读取文件access
	 * 
	 * @param filePath
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static void readFileACCESS(File mdbFile, String sql) {
		Properties prop = new Properties();
		prop.put("charSet", "gb2312"); // 这里是解决中文乱码
		prop.put("user", "");
		prop.put("password", "");
		String url = "jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};DBQ="
				+ mdbFile.getAbsolutePath();
		Statement stmt = null;
		ResultSet rs = null;
		String tableName = null;
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			// 连接到mdb文件
			Connection conn = DriverManager.getConnection(url, prop);
			ResultSet tables = conn.getMetaData().getTables(
					mdbFile.getAbsolutePath(), null, null,
					new String[] { "TABLE" });
			// 获取第一个表名
			if (tables.next()) {
				tableName = tables.getString(3);// getXXX can only be used once
			} else {
				return;
			}
			stmt = (Statement) conn.createStatement();
			// 读取第一个表的内容
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
					FileOutputStream fs = new FileOutputStream(txtFile, true); // 在该文件的末尾添加内容

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
				// 获得所有列的数目及实际列数
				int columnCount2 = data.getColumnCount();
				// 获得指定列的列名
				String columnName = data.getColumnName(i);
				// 获得指定列的列值
				int columnType = data.getColumnType(i);
				// 获得指定列的数据类型名
				String columnTypeName = data.getColumnTypeName(i);
				// 所在的Catalog名字
				String catalogName = data.getCatalogName(i);
				// 对应数据类型的类
				String columnClassName = data.getColumnClassName(i);
				// 在数据库中类型的最大字符个数
				int columnDisplaySize = data.getColumnDisplaySize(i);
				// 默认的列的标题
				String columnLabel = data.getColumnLabel(i);
				// 获得列的模式
				String schemaName = data.getSchemaName(i);
				// 某列类型的精确度(类型的长度)
				int precision = data.getPrecision(i);
				// 小数点后的位数
				int scale = data.getScale(i);
				// 获取某列对应的表名
				String tableName2 = data.getTableName(i);
				// 是否自动递增
				boolean isAutoInctement = data.isAutoIncrement(i);
				// 在数据库中是否为货币型
				boolean isCurrency = data.isCurrency(i);
				// 是否为空
				int isNullable = data.isNullable(i);
				// 是否为只读
				boolean isReadOnly = data.isReadOnly(i);
				// 能否出现在where中
				boolean isSearchable = data.isSearchable(i);
				System.out.println("[" + df.format(new Date())
						+ " data.getColumnCount()=" + data.getColumnCount());
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "的字段名称:" + columnName);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "的类型,返回SqlType中的编号:" + columnType);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "的数据类型名:" + columnTypeName);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "所在的Catalog名字:" + catalogName);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "对应数据类型的类:" + columnClassName);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "在数据库中类型的最大字符个数:" + columnDisplaySize);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "的默认的列的标题:" + columnLabel);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "的模式:" + schemaName);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "类型的精确度(类型的长度):" + precision);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "小数点后的位数:" + scale);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "对应的表名:" + data.getTableName(1));
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "是否自动递增:" + isAutoInctement);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "在数据库中是否为货币型:" + isCurrency);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "是否为空:" + isNullable);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "是否为只读:" + isReadOnly);
				System.out.println("[" + df.format(new Date()) + " 获得列" + i
						+ "能否出现在where中:" + isSearchable);
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
		int bfType = 0x424d; // 位图文件类型（0—1字节）
		int bfSize = 54 + 1024 + w * nHeight;// bmp文件的大小（2—5字节）
		int bfReserved1 = 0;// 位图文件保留字，必须为0（6-7字节）
		int bfReserved2 = 0;// 位图文件保留字，必须为0（8-9字节）
		int bfOffBits = 54 + 1024;// 文件头开始到位图实际数据之间的字节的偏移量（10-13字节）

		dos.writeShort(bfType); // 输入位图文件类型'BM'
		dos.write(changeByte(bfSize), 0, 4); // 输入位图文件大小
		dos.write(changeByte(bfReserved1), 0, 2);// 输入位图文件保留字
		dos.write(changeByte(bfReserved2), 0, 2);// 输入位图文件保留字
		dos.write(changeByte(bfOffBits), 0, 4);// 输入位图文件偏移量

		int biSize = 40;// 信息头所需的字节数（14-17字节）
		int biWidth = nWidth;// 位图的宽（18-21字节）
		int biHeight = nHeight;// 位图的高（22-25字节）
		int biPlanes = 1; // 目标设备的级别，必须是1（26-27字节）
		int biBitcount = 8;// 每个像素所需的位数（28-29字节），必须是1位（双色）、4位（16色）、8位（256色）或者24位（真彩色）之一。
		int biCompression = 0;// 位图压缩类型，必须是0（不压缩）（30-33字节）、1（BI_RLEB压缩类型）或2（BI_RLE4压缩类型）之一。
		int biSizeImage = w * nHeight;// 实际位图图像的大小，即整个实际绘制的图像大小（34-37字节）
		int biXPelsPerMeter = 0;// 位图水平分辨率，每米像素数（38-41字节）这个数是系统默认值
		int biYPelsPerMeter = 0;// 位图垂直分辨率，每米像素数（42-45字节）这个数是系统默认值
		int biClrUsed = 0;// 位图实际使用的颜色表中的颜色数（46-49字节），如果为0的话，说明全部使用了
		int biClrImportant = 0;// 位图显示过程中重要的颜色数(50-53字节)，如果为0的话，说明全部重要

		dos.write(changeByte(biSize), 0, 4);// 输入信息头数据的总字节数
		dos.write(changeByte(biWidth), 0, 4);// 输入位图的宽
		dos.write(changeByte(biHeight), 0, 4);// 输入位图的高
		dos.write(changeByte(biPlanes), 0, 2);// 输入位图的目标设备级别
		dos.write(changeByte(biBitcount), 0, 2);// 输入每个像素占据的字节数
		dos.write(changeByte(biCompression), 0, 4);// 输入位图的压缩类型
		dos.write(changeByte(biSizeImage), 0, 4);// 输入位图的实际大小
		dos.write(changeByte(biXPelsPerMeter), 0, 4);// 输入位图的水平分辨率
		dos.write(changeByte(biYPelsPerMeter), 0, 4);// 输入位图的垂直分辨率
		dos.write(changeByte(biClrUsed), 0, 4);// 输入位图使用的总颜色数
		dos.write(changeByte(biClrImportant), 0, 4);// 输入位图使用过程中重要的颜色数

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
		// "&" 与（AND），对两个整型操作数中对应位执行布尔代数，两个位都为1时输出1，否则0。
		abyte[0] = (byte) (0xff & number);
		// ">>"右移位，若为正数则高位补0，若为负数则高位补1
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
