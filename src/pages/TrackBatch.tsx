import { useParams, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, Package, MapPin, Calendar, Hash, Shield, CheckCircle2, DollarSign } from "lucide-react";
import { useState, useEffect } from "react";
import { consumerAPI, type VerificationResponse } from "@/lib/api";
import { useToast } from "@/hooks/use-toast";

const TrackBatch = () => {
  const { batchId } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [data, setData] = useState<VerificationResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (batchId) {
      loadBatchData();
    }
  }, [batchId]);

  const loadBatchData = async () => {
    try {
      setLoading(true);
      const result = await consumerAPI.verify(parseInt(batchId!));
      setData(result);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load batch data",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading batch information...</p>
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle>Batch Not Found</CardTitle>
            <CardDescription>The batch you're looking for doesn't exist.</CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/dashboard")}>Return to Dashboard</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b border-border bg-card shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <Button variant="ghost" onClick={() => navigate(-1)} className="mb-2">
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back
          </Button>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary/10 rounded-lg">
              <Package className="w-6 h-6 text-primary" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-foreground">Batch Tracking</h1>
              <p className="text-sm text-muted-foreground font-mono">{data.batch.batchNumber}</p>
            </div>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <div className="grid gap-6 md:grid-cols-4 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Product</CardTitle>
              <Package className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-lg font-bold">{data.product.name}</div>
              <p className="text-xs text-muted-foreground">{data.batch.quantity} units</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Current Status</CardTitle>
              <MapPin className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <Badge className="bg-success text-success-foreground">{data.batch.status}</Badge>
              <p className="text-xs text-muted-foreground mt-1">{data.batch.currentLocation}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Events</CardTitle>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-lg font-bold">{data.events.length}</div>
              <p className="text-xs text-muted-foreground">Blockchain events</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Verification</CardTitle>
              <Shield className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <CheckCircle2 className={`w-5 h-5 ${data.allEventsVerified ? 'text-blockchain-verified' : 'text-muted-foreground'}`} />
                <span className="text-sm font-medium">{data.allEventsVerified ? 'Verified' : 'Pending'}</span>
              </div>
              <p className="text-xs text-muted-foreground mt-1">All events on-chain</p>
            </CardContent>
          </Card>
        </div>

        {data.paymentAmount && (
          <Card className="mb-8">
            <CardHeader>
              <div className="flex items-center gap-2">
                <DollarSign className="w-5 h-5 text-primary" />
                <CardTitle>Payment Information</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid gap-2">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Status:</span>
                  <Badge>{data.paymentStatus}</Badge>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Amount:</span>
                  <span className="font-semibold">${data.paymentAmount}</span>
                </div>
                {data.paymentTxHash && (
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">TX Hash:</span>
                    <span className="font-mono text-xs">{data.paymentTxHash}</span>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader>
            <CardTitle>Supply Chain Journey</CardTitle>
            <CardDescription>Complete history of batch movement with blockchain verification</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {data.events.map((event, index) => (
                <div key={event.id} className="relative pl-8 pb-6 border-l-2 border-primary/20 last:border-l-0 last:pb-0">
                  <div className="absolute left-0 top-0 -translate-x-1/2 w-4 h-4 rounded-full bg-primary border-4 border-background" />
                  
                  <div className="space-y-3">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <h3 className="font-semibold text-foreground">
                          {event.fromPartyEmail} → {event.toPartyEmail}
                        </h3>
                        <p className="text-sm text-muted-foreground flex items-center gap-2 mt-1">
                          <MapPin className="w-3 h-3" />
                          {event.location}
                        </p>
                      </div>
                      <Badge className="bg-success text-success-foreground">
                        {event.status.replace("_", " ")}
                      </Badge>
                    </div>

                    <div className="grid gap-2 text-sm">
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <Calendar className="w-4 h-4" />
                        <span>{new Date(event.timestamp).toLocaleString()}</span>
                      </div>
                      
                      <div className="flex items-center gap-2 font-mono text-xs bg-muted/50 p-2 rounded">
                        <Hash className="w-4 h-4 text-muted-foreground" />
                        <span className="text-muted-foreground">TX:</span>
                        <span className="text-foreground">{event.txHash}</span>
                      </div>
                      
                      <div className="flex items-center gap-2 font-mono text-xs bg-muted/50 p-2 rounded">
                        <span className="text-muted-foreground">Block:</span>
                        <span className="text-foreground">{event.blockNumber}</span>
                        {event.verified && (
                          <Badge variant="outline" className="ml-auto border-blockchain-verified text-blockchain-verified">
                            <CheckCircle2 className="w-3 h-3 mr-1" />
                            Verified
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </main>
    </div>
  );
};

export default TrackBatch;
