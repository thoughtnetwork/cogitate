package live.thought.cogitate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestException;
import java.security.MessageDigest;

final class Util
{
  static final int BLOCKSIZE = 8 * 1024;

  public interface IOutput
  {
    void writeBlock(byte[] block, int blockSize) throws IOException;
  }

  public static long BlockCopy(InputStream from, IOutput to) throws IOException
  {
    long bytesCopied = 0;
    byte buffer[] = new byte[BLOCKSIZE];
    while (true)
    {
      int actualRead = from.read(buffer);
      if (actualRead == -1)
      {
        return bytesCopied;
      }
      bytesCopied += actualRead;
      to.writeBlock(buffer, actualRead);
    }
  }

  public static long CopyStream(InputStream from, OutputStream to) throws IOException
  {
    return BlockCopy(from, (block, size) -> to.write(block, 0, size));
  }

  public static String HexDigest(MessageDigest digest) throws DigestException
  {
    byte[] digestBytes = digest.digest();
    StringBuilder builder = new StringBuilder(2 * digestBytes.length);
    for (int i = 0; i < digestBytes.length; i++)
    {
      int val = Byte.toUnsignedInt(digestBytes[i]);
      if (val < 0x10)
      {
        builder.append('0');
      }
      builder.append(Integer.toHexString(val));
    }
    return builder.toString();
  }
}
