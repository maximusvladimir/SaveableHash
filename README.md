SaveableHash
============

A little Java library that lets you save HashMaps to a file, etc.


How to Use
==========

There is a package (```com.maximusvladimir.saveablehash.test```) that has various examples, but I will discuss some features here:

## Basics

So lets say you want to save your HashMap to a file:

```Java

HashMap<String, Long> millisecondTimeFrames = new HashMap<String, Long>();
millisecondTimeFrames.put("phase1", 2048578L);
millisecondTimeFrames.put("schedule", 928034592L);
...
FileWriter fr = new FileWriter(new File("timeframes.txt"));
BufferedWriter writer = new BufferedWriter(fr);
HashIO.save(millisecondTimeFrames, writer);
// Don't forget to close your stuff here. :wink:

```

```timeframes.txt``` would then hopefully look something like this:

```
com.maximusvladimir.saveablehash
java.lang.Long:schedule:928034592
java.lang.Long:phase1:2048578
java.lang.Long:overhead:4929491905283
...
```

To read the HashMap you would need to use ```HashIO.load()```.

```Java
FileReader fr = new FileReader(new File("timeframes.txt"));
BufferedReader reader = new BufferedReader(fr);
HashIO.load(millisecondTimeFrames, reader);
```

## A little bit more

Now lets say you want to introduce a little structure called ```Timestamp```.

```Java
class Timestamp {
			public long start;
			public long end;
			public Timestamp() {
				
			}
			
			public void setStart(long s) {
				start = s;
			}
			
			public long getStart() {
				return start;
			}
			
			public void setEnd(long e) {
				end = e;
			}
			
			public long getEnd() {
				return end;
			}
		}
```

Now how can we do it?

If you have a very demiurgic mind, you are probably wondering how on Earth this would work. After all, how does this little library know how to parse that. Well it doesn't (not yet at least, may be in the near future).

So this is how we do it:

```Java
ParseFactory factory = new ParseFactory();
factory.add(Timestamp.class, new IOSerialize<Timestamp>() {
  public <T> T load(ParseFactory factory, String data) {
    Timestamp stamp = new Timestamp();
    String[] parts = data.split(",");
    stamp.setStart(Long.parseLong(parts[0]));
		stamp.setEnd(Long.parseLong(parts[1]));
    return (T)stamp;
  }

  public <T> String save(ParseFactory factory, T obj) {
    return ((Timestamp)obj).getStart() + "," + ((Timestamp)obj).getEnd();
  }
});
```

Also you will need to do the following:

```Java
...
HashIO.save(factory, millisecondTimeFrames, writer);
...
HashIO.load(factory, millisecondTimeFrames, reader);
...
```

In the future, as I mentioned earlier, I would like to do something like this to add the class:

```Java
factory.add(Timestamp.class);
```


Anyways I hope this was helpful.
