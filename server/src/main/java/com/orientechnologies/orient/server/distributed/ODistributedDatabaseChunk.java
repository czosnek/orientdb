/*
 * Copyright 2010-2014 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.server.distributed;

import com.orientechnologies.common.io.OFileUtils;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ODistributedDatabaseChunk implements Externalizable {
  public String  filePath;
  public long    offset;
  public byte[]  buffer;
  public boolean last;

  public ODistributedDatabaseChunk() {
  }

  public ODistributedDatabaseChunk(final File iFile, final long iOffset, final int iMaxSize) throws IOException {
    filePath = iFile.getAbsolutePath();
    offset = iOffset;

    final long fileSize = iFile.length();
    if (offset > fileSize)
      throw new IllegalArgumentException("Offset " + iOffset + " cannot be bigger then the file itself: "
          + OFileUtils.getSizeAsString(fileSize));

    final FileInputStream in = new FileInputStream(iFile);

    final int toRead = (int) Math.min(iMaxSize, fileSize - offset);
    buffer = new byte[toRead];

    try {
      in.skip(offset);
      in.read(buffer);
      last = offset + toRead >= fileSize;

    } finally {
      try {
        in.close();
      } catch (IOException e) {
      }
    }
  }

  @Override
  public String toString() {
    return filePath + "[" + offset + "-" + buffer.length + "] (last=" + last + ")";
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeUTF(filePath);
    out.writeLong(offset);
    out.writeInt(buffer.length);
    out.write(buffer);
    out.writeBoolean(last);
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    filePath = in.readUTF();
    offset = in.readLong();
    final int size = in.readInt();
    buffer = new byte[size];
    in.readFully(buffer);
    last = in.readBoolean();
  }
}
