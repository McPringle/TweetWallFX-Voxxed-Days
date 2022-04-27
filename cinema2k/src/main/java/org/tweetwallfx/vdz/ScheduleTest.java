/*
 * The MIT License
 *
 * Copyright 2015-2018 TweetWallFX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.tweetwallfx.vdz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tweetwallfx.devoxx.api.cfp.client.CFPClient;
import org.tweetwallfx.devoxx.api.cfp.client.Rooms;
import org.tweetwallfx.devoxx.api.cfp.client.Schedule;

public class ScheduleTest {
    private static final Logger LOG = LogManager.getLogger(ScheduleTest.class);

    public static void main(String[] args) {
        LOG.info("reading event..");
        var client = CFPClient.getClient();
//        client.getRooms().ifPresent(ScheduleTest::rooms);
        client.getSchedule("tuesday").ifPresent(ScheduleTest::schedule);
    }

    private static void rooms(Rooms rooms) {
        LOG.info("rooms: {}", rooms);
    }

    private static void schedule(Schedule schedule) {
        LOG.info("schedule: {}", schedule);
    }
}
