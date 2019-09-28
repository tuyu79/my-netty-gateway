package com.turan.service;

import com.turan.api.UniqSequenceApi;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UniqSequenceService implements UniqSequenceApi
{
    // TODO turan 分布式获取
    private AtomicInteger sequence = new AtomicInteger(0);

    @Override
    public int nextId()
    {
        if (sequence.get() == Integer.MAX_VALUE)
        {
            synchronized (this)
            {
                if (sequence.get() == Integer.MAX_VALUE)
                {
                    sequence.set(0);
                }
            }
        }
        return sequence.getAndIncrement();
    }
}
