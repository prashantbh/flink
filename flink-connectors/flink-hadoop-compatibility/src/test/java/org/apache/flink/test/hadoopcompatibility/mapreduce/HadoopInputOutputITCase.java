/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.test.hadoopcompatibility.mapreduce;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.java.hadoop.mapreduce.HadoopInputFormat;
import org.apache.flink.api.java.hadoop.mapreduce.HadoopOutputFormat;
import org.apache.flink.test.hadoopcompatibility.mapreduce.example.WordCount;
import org.apache.flink.test.testdata.WordCountData;
import org.apache.flink.test.util.JavaProgramTestBase;
import org.apache.flink.util.OperatingSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import static org.apache.flink.test.util.TestBaseUtils.compareResultsByLinesInMemory;
import static org.assertj.core.api.Assumptions.assumeThat;

/** IT cases for both the {@link HadoopInputFormat} and {@link HadoopOutputFormat}. */
// This test case has been updated from dataset to a datastream.
// It is essentially a batch job, but the HadoopInputFormat is an unbounded source.
// As a result, the test case cannot be set to batch runtime mode and should not run with the
// adaptive scheduler.
@Tag("org.apache.flink.testutils.junit.FailsWithAdaptiveScheduler")
class HadoopInputOutputITCase extends JavaProgramTestBase {

    private String textPath;
    private String resultPath;

    @BeforeEach
    void checkOperatingSystem() {
        // FLINK-5164 - see https://wiki.apache.org/hadoop/WindowsProblems
        assumeThat(OperatingSystem.isWindows())
                .as("This test can't run successfully on Windows.")
                .isFalse();
    }

    @Override
    protected void preSubmit() throws Exception {
        textPath = createTempFile("text.txt", WordCountData.TEXT);
        resultPath = getTempDirPath("result");
    }

    @Override
    protected void postSubmit() throws Exception {
        compareResultsByLinesInMemory(WordCountData.COUNTS, resultPath, new String[] {".", "_"});
    }

    @Override
    protected JobExecutionResult testProgram() throws Exception {
        return WordCount.run(new String[] {textPath, resultPath});
    }
}
